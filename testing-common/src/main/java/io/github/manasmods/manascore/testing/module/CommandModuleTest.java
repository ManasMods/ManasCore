/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.command.api.*;
import io.github.manasmods.manascore.command.api.parameter.Enum;
import io.github.manasmods.manascore.command.api.parameter.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class CommandModuleTest {
    private static final Component RESPONSE = Component.literal("Works!");

    public static void init() {
        CommandArgumentRegistrationEvent.EVENT.register(registry -> {
            registry.registerEnum(TestEnum.class);
        });

        CommandRegistry.registerCommand(TestCommand.class);
    }

    @Command(value = "foo", subCommands = {TestSubCommand.class})
    public static class TestCommand {
        @Execute
        public boolean withPerms(@Sender CommandSourceStack sender) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }
    }

    @Command(value = "bar")
    public static class TestSubCommand {
        @Permission("manascore.command.test")
        @Execute
        public boolean withPerms(@Sender CommandSourceStack sender, @Literal("perms") String l) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }

        @Execute
        public boolean uuidArg(@Sender CommandSourceStack sender, @Literal("uuid") String l, @Uuid UUID uuid) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }

        @Execute
        public boolean enumArg(@Sender CommandSourceStack sender, @Literal("enum") String l, @Enum(TestEnum.class) TestEnum _enum) {
            sender.sendSystemMessage(RESPONSE);
            return true;
        }

        @Execute
        public boolean boolArg(@Sender CommandSourceStack sender, @Literal("bool") String l, @Bool Boolean bool) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nBoolean: '")
                    .append(bool ? "true" : "false")
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveBoolArg(@Sender CommandSourceStack sender, @Literal("primitive_bool") String l, @Bool boolean bool) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nBoolean (primitive): '")
                    .append(bool ? "true" : "false")
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean wordArg(@Sender CommandSourceStack sender, @Literal("word") String l, @Text String word) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nWord: '")
                    .append(word)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean stringArg(@Sender CommandSourceStack sender, @Literal("string") String l, @Text(Text.Type.STRING) String string) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nString: '")
                    .append(string)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean greedyStringArg(@Sender CommandSourceStack sender, @Literal("greedy_string") String l, @Text(Text.Type.GREEDY_STRING) String greedyString) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nGreedy String: '")
                    .append(greedyString)
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean doubleArg(@Sender CommandSourceStack sender, @Literal("double") String l, @DoubleNumber Double d) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nDouble: '")
                    .append(String.valueOf(d))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveDoubleArg(@Sender CommandSourceStack sender, @Literal("primitive_double") String l, @DoubleNumber double d) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nDouble (primitive): '")
                    .append(String.valueOf(d))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean floatArg(@Sender CommandSourceStack sender, @Literal("float") String l, @FloatNumber Float f) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nFloat: '")
                    .append(String.valueOf(f))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveFloatArg(@Sender CommandSourceStack sender, @Literal("primitive_float") String l, @FloatNumber float f) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nFloat (primitive): '")
                    .append(String.valueOf(f))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean intArg(@Sender CommandSourceStack sender, @Literal("int") String l, @IntNumber Integer i) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nInteger: '")
                    .append(String.valueOf(i))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveIntArg(@Sender CommandSourceStack sender, @Literal("primitive_int") String l, @IntNumber int i) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nInteger (primitive): '")
                    .append(String.valueOf(i))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean longArg(@Sender CommandSourceStack sender, @Literal("long") String l, @LongNumber Long l1) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nLong: '")
                    .append(String.valueOf(l1))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean primitiveLongArg(@Sender CommandSourceStack sender, @Literal("primitive_long") String l, @LongNumber long l1) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nLong (primitive): '")
                    .append(String.valueOf(l1))
                    .append("'")
            );
            return true;
        }

        @Execute
        public boolean multiple(@Sender CommandSourceStack sender, @Literal("literal") String l, @Enum(TestEnum.class) TestEnum _enum) {
            sender.sendSystemMessage(RESPONSE.copy()
                    .append("\nLiteral: '")
                    .append(Component.literal(l))
                    .append("'\nEnum: '")
                    .append(_enum.name())
                    .append("'")
            );
            return true;
        }
    }

    public enum TestEnum {
        TEST,
        TEST2
    }
}
