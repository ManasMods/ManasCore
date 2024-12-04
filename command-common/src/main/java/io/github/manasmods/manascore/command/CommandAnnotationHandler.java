/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.api.parameter.Sender;
import io.github.manasmods.manascore.command.internal.CommandArgumentRegistry;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@ApiStatus.Internal
public class CommandAnnotationHandler {
    static final List<CommandNode> COMMANDS = new ArrayList<>();

    public static <T> void registerCommand(Class<T> commandClass, Supplier<T> factory) {
        var rootAnnotation = requireCommandAnnotation(commandClass);
        validateSubCommands(rootAnnotation);

        COMMANDS.add(new CommandNode(
                commandClass,
                factory.get(),
                rootAnnotation.value(),
                Arrays.stream(commandClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Execute.class)).toList(),
                rootAnnotation.subCommands()
        ));
    }

    /**
     * Require the @Command annotation on a class
     */
    public static Command requireCommandAnnotation(Class<?> commandClass) {
        if (!commandClass.isAnnotationPresent(Command.class)) {
            throw new IllegalArgumentException(String.format("Class %s must be annotated with @Command", commandClass.getName()));
        }

        var commandAnnotation = commandClass.getAnnotation(Command.class);

        if (commandAnnotation.value().length == 0) {
            throw new IllegalArgumentException(String.format("Command Annotation at Class %s must have at least one root node", commandClass.getName()));
        }

        return commandAnnotation;
    }

    /**
     * Recursively validate subcommand classes
     */
    public static void validateSubCommands(Command commandAnnotation) {
        for (var subCommandClass : commandAnnotation.subCommands()) {
            var subCommandAnnotation = requireCommandAnnotation(subCommandClass);
            validateSubCommands(subCommandAnnotation);
        }
    }

    @RequiredArgsConstructor
    public static class CommandNode {
        private final Class<?> commandClass;
        private final Object commandClassInstance;
        private final String[] nodeLiterals;
        private final List<Method> executors;
        private final Class<?>[] subCommandClasses;
        private final Map<String, Permission> permissionNodes = new HashMap<>();


        public List<LiteralArgumentBuilder<CommandSourceStack>> build(CommandArgumentRegistry argumentRegistry) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            AtomicReference<Consumer<LiteralArgumentBuilder<CommandSourceStack>>> rootExecutor = new AtomicReference<>(builder -> {
            });
            // Create ArgumentBuilders for each executor in the command class
            var arguments = executors.stream().flatMap(method -> {
                        if (!method.getReturnType().isAssignableFrom(boolean.class) && !method.getReturnType().isAssignableFrom(Boolean.class)) {
                            throw new RuntimeException("Method %s in %s has a return type that is not a boolean".formatted(method.getName(), this.commandClass.getName()));
                        }

                        var types = method.getParameterTypes();
                        var parameters = method.getParameters();
                        var allowsConsole = new AtomicBoolean(true);
                        // Node hierarchy (Ordered from first param -> last param)
                        List<List<ArgumentBuilder<CommandSourceStack, ?>>> nodeArgumentHierarchy = new ArrayList<>();
                        // List of ArgumentBuilders for each parameter
                        List<ArgumentBuilder<CommandSourceStack, ?>> lastParameterNodes = new ArrayList<>();
                        // List of ParameterSuppliers for each parameter (out list index = parameter index, inner list index = argument index)
                        List<List<ParameterSupplier<Object>>> parameterArgumentSuppliers = new ArrayList<>();

                        for (int i = 0; i < types.length; i++) {
                            final var argumentType = types[i];
                            final var parameter = parameters[i];
                            final List<ArgumentBuilder<CommandSourceStack, ?>> newNodes = new ArrayList<>();
                            boolean isSenderArgument = false;
                            var parameterAnnotations = parameter.getAnnotations();

                            final var finalizedI = i;
                            final var argumentNodeHandler = new CommandArgumentRegistry.ArgumentNodeHandler(
                                    newNodes,
                                    (index, value) -> addValueTo2DList(parameterArgumentSuppliers, index, finalizedI, value),
                                    value -> addValueTo2DList(parameterArgumentSuppliers, value),
                                    allowsConsole,
                                    parameter.getName()
                            );

                            for (var parameterAnnotation : parameterAnnotations) {
                                var argumentFactory = (CommandArgumentRegistry.CommandArgumentFactory) argumentRegistry.get(argumentType, parameterAnnotation.annotationType());
                                // Skip if no factory is found
                                if (argumentFactory == null) {
                                    ManasCoreCommand.LOG.debug("No ArgumentFactory found for Parameter Annotation {} in Method {} in {} at index {}", parameterAnnotation.getClass().getName(), method.getName(), this.commandClass.getName(), i);
                                    continue;
                                }

                                if (parameterAnnotation instanceof Sender) {
                                    isSenderArgument = true;
                                }

                                argumentFactory.create(parameterAnnotation, argumentNodeHandler);
                            }

                            if (newNodes.isEmpty() && !isSenderArgument) {
                                throw new RuntimeException("Parameter at index %s in Method %s in %s is not annotated".formatted(i, method.getName(), this.commandClass.getName()));
                            }

                            // Set the new node as the last node
                            if (!isSenderArgument) {
                                lastParameterNodes = newNodes;
                                nodeArgumentHierarchy.add(newNodes);
                            }
                        }

                        // No parameters found
                        if (nodeArgumentHierarchy.isEmpty()) {
                            if (parameterArgumentSuppliers.size() == 1) {
                                rootExecutor.set(builder -> commandExecuteApplier(builder, method, parameterArgumentSuppliers.get(0)));
                            }

                            return Stream.empty();
                        }

                        // Add Permission check and execution to the last node
                        final var allowsConsoleFinal = allowsConsole.get();
                        for (int i = 0; i < lastParameterNodes.size(); i++) {
                            var builder = lastParameterNodes.get(i);
                            final var argumentSuppliers = parameterArgumentSuppliers.get(i);

                            // Handle Permissions
                            if (method.isAnnotationPresent(Permission.class)) {
                                var permission = method.getAnnotation(Permission.class);
                                permissionNodes.put(permission.value(), permission);

                                builder.requires(commandSourceStack -> {
                                    if (commandSourceStack.getPlayer() == null) return allowsConsoleFinal;
                                    return PlatformCommandUtils.hasPermission(commandSourceStack, permission);
                                });
                            }


                            commandExecuteApplier(builder, method, argumentSuppliers);
                        }

                        // Get root node
                        var argumentNodesIterator = nodeArgumentHierarchy.listIterator(nodeArgumentHierarchy.size());

                        while (true) {
                            // Get current nodes
                            var currentArgumentNodes = argumentNodesIterator.previous();
                            // Check if we are at the root node
                            if (!argumentNodesIterator.hasPrevious()) {
                                // no more parents that need children
                                return currentArgumentNodes.stream();
                            }
                            // Get parent nodes
                            var parentArgumentNodes = argumentNodesIterator.previous();
                            // Add current nodes to parent nodes
                            parentArgumentNodes.forEach(parent -> currentArgumentNodes.forEach(parent::then));
                            // Set the parent nodes as the current nodes
                            argumentNodesIterator.next();
                        }
                    })
                    .toList();
            // Create build CommandNodes for each subCommand
            var subCommands = new ArrayList<LiteralArgumentBuilder<CommandSourceStack>>();
            for (Class<?> subCommandClass : subCommandClasses) {
                if (!subCommandClass.isAnnotationPresent(Command.class)) {
                    ManasCoreCommand.LOG.error("Class {} is not annotated with @Command", subCommandClass.getName());
                    continue;
                }

                Command subCommandAnnotation = subCommandClass.getAnnotation(Command.class);

                var subCommandNodes = subCommandAnnotation.value();
                if (subCommandNodes.length == 0) {
                    ManasCoreCommand.LOG.error("Class {} is annotated with @Command but no root nodes are defined", subCommandClass.getName());
                    continue;
                }

                try {
                    var instance = subCommandClass.getDeclaredConstructor().newInstance();
                    CommandNode subCommandNode = new CommandNode(subCommandClass, instance, subCommandNodes, Arrays.stream(subCommandClass.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(Execute.class)).toList(), subCommandAnnotation.subCommands());
                    subCommands.addAll(subCommandNode.build(argumentRegistry));
                    subCommandNode.permissionNodes.forEach(permissionNodes::putIfAbsent);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            Permission rootPermissionNode = null;
            if (this.commandClass.isAnnotationPresent(Permission.class)) {
                var permission = this.commandClass.getAnnotation(Permission.class);

                permissionNodes.put(permission.value(), permission);
                rootPermissionNode = permission;
            }
            final var rootPermissionNodeFinal = rootPermissionNode;


            // Finalize the CommandNodes
            return Arrays.stream(this.nodeLiterals).map(literal -> {
                ManasCoreCommand.LOG.debug("Creating Command {} with {} arguments and {} subCommands", literal, arguments.size(), subCommands.size());
                // Create root node
                var node = Commands.literal(literal);
                rootExecutor.get().accept(node);
                // Add Permissions
                if (rootPermissionNodeFinal != null) node.requires(commandSourceStack -> {
                    if (commandSourceStack.getPlayer() == null) return true;
                    return PlatformCommandUtils.hasPermission(commandSourceStack, rootPermissionNodeFinal);
                });
                // Add Arguments
                arguments.forEach(node::then);
                // Add SubCommands
                subCommands.forEach(node::then);

                return node;
            }).toList();
        }

        public Collection<Permission> getPermissions() {
            return permissionNodes.values();
        }

        public interface ParameterSupplier<R> {
            R get(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
        }

        private static <T> List<T> getOrCreateIndexWithClone(List<List<T>> outerList, int outerListIndex) {
            List<T> innerList;

            // Get or create the inner list
            if (outerList.size() <= outerListIndex) {
                innerList = new ArrayList<>();
                // Fill the new list with existing values
                if (!outerList.isEmpty()) {
                    innerList.addAll(outerList.get(0));
                }

                outerList.add(outerListIndex, innerList);
            } else {
                innerList = outerList.get(outerListIndex);
            }

            return innerList;
        }

        private static <T> void addValueTo2DList(List<List<T>> outerList, int outerListIndex, int innerListIndex, T value) {
            List<T> innerList = getOrCreateIndexWithClone(outerList, outerListIndex);

            // Add the value to the inner list or replace the value
            if (innerList.size() <= innerListIndex) {
                innerList.add(innerListIndex, value);
            } else {
                innerList.set(innerListIndex, value);
            }
        }

        private static <T> void addValueTo2DList(List<List<T>> outerList, T value) {
            // Ensure to add the value at least one time
            if (outerList.isEmpty()) {
                outerList.add(new ArrayList<>());
            }
            // Add the value to all inner lists
            outerList.forEach(innerList -> innerList.add(value));
        }

        private void commandExecuteApplier(ArgumentBuilder<CommandSourceStack, ?> builder, Method targetMetod, List<ParameterSupplier<Object>> parameterSuppliers) {
            builder.executes(commandContext -> {
                var args = new Object[parameterSuppliers.size()];
                for (int j = 0; j < parameterSuppliers.size(); j++) {
                    args[j] = parameterSuppliers.get(j).get(commandContext);
                }

                int flag = 0;

                try {
                    if ((boolean) targetMetod.invoke(this.commandClassInstance, args)) {
                        flag = com.mojang.brigadier.Command.SINGLE_SUCCESS;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                return flag;
            });
        }
    }
}
