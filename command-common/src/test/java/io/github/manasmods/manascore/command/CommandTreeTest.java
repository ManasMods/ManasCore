/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.manasmods.manascore.command.api.Command;
import io.github.manasmods.manascore.command.api.CommandArgumentRegistrationEvent;
import io.github.manasmods.manascore.command.api.Execute;
import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.api.Permission.PermissionLevel;
import io.github.manasmods.manascore.command.api.parameter.SenderArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.IntegerArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.LiteralArg;
import io.github.manasmods.manascore.command.api.parameter.primitive.TextArg;
import io.github.manasmods.manascore.command.internal.CommandArgumentRegistry;
import io.github.manasmods.manascore.command.internal.PermissionRequirement;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandTreeTest {
    private static final CommandArgumentRegistry REGISTRY = new CommandArgumentRegistry();
    private static final CommandSourceStack CONSOLE = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, 4, "test", Component.literal("test"), null, null);
    private static final Comparator<Method> BY_NAME = Comparator.comparing(Method::getName);
    private static final List<List<String>> BANK_ORDERS = List.of(
            List.of("deposit", "depositNote", "depositTag"),
            List.of("deposit", "depositTag", "depositNote"),
            List.of("depositNote", "deposit", "depositTag"),
            List.of("depositNote", "depositTag", "deposit"),
            List.of("depositTag", "deposit", "depositNote"),
            List.of("depositTag", "depositNote", "deposit")
    );
    private static @Nullable String executed = null;

    @BeforeAll
    static void setUp() {
        ManasCoreCommand.init();
        CommandArgumentRegistrationEvent.EVENT.invoker().register(REGISTRY, new CommandDispatcher<>(), null);
    }

    @AfterEach
    void resetOrder() {
        CommandAnnotationHandler.executorOrder = null;
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void shorterGatedOverloadKeepsItsGate(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(EcoCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of("test.eco.give"), "give", "player", "amount");
        this.assertNode(root, true, true, List.of(), "give", "player", "amount", "currency");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void longerGatedOverloadLeavesShorterOpen(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(PayCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of(), "send", "player", "amount");
        this.assertNode(root, true, true, List.of("test.pay.note"), "send", "player", "amount", "note");
    }

    @Test
    void gatesDoNotDependOnMethodOrder() throws Exception {
        Map<String, String> eco = this.getGateMap(this.getBuilt(EcoCommand.class, BY_NAME));
        Map<String, String> bank = this.getGateMap(this.getBuilt(BankCommand.class, this.getNamedOrder(BANK_ORDERS.get(0))));
        List<Executable> checks = new ArrayList<>();
        checks.add(() -> assertEquals(Map.of(
                "eco", "open []",
                "eco give", "open []",
                "eco give player", "open []",
                "eco give player amount", "open command [test.eco.give]",
                "eco give player amount currency", "open command []"
        ), eco, "eco baseline"));
        checks.add(() -> assertEquals(Map.of(
                "bank", "open []",
                "bank deposit", "open []",
                "bank deposit amount", "open command [test.bank.deposit]",
                "bank deposit amount note", "open command []",
                "bank deposit amount note tag", "open command [test.bank.tag]"
        ), bank, "bank baseline"));
        checks.add(() -> assertEquals(eco, this.getGateMap(this.getBuilt(EcoCommand.class, BY_NAME.reversed())), "eco reversed"));
        for (List<String> names : BANK_ORDERS) checks.add(() -> assertEquals(bank, this.getGateMap(this.getBuilt(BankCommand.class, this.getNamedOrder(names))), "bank order " + names));
        assertAll(checks);
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void aliasesKeepTheirGates(boolean reversed) throws Exception {
        List<LiteralCommandNode<CommandSourceStack>> roots = this.getBuilt(ShopCommand.class, this.getOrder(reversed));
        assertEquals(2, roots.size());
        for (LiteralCommandNode<CommandSourceStack> root : roots) {
            this.assertNode(root, false, true, List.of("test.shop"));
            this.assertNode(root, true, true, List.of("test.shop.sell"), "sell", "item", "amount");
            this.assertNode(root, true, true, List.of(), "sell", "item", "amount", "price");
        }
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void subCommandKeepsItsGates(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(AdminCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, false, true, List.of());
        this.assertNode(root, false, true, List.of("test.admin.eco"), "eco");
        this.assertNode(root, true, true, List.of("test.admin.eco.take"), "eco", "take", "player", "amount");
        this.assertNode(root, true, true, List.of(), "eco", "take", "player", "amount", "reason");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void walletPlainFormStaysOpen(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(WalletCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of(), "balance");
        this.assertNode(root, true, true, List.of("test.wallet.others"), "balance", "player");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void samePathDifferentPermissionsThrows(boolean reversed) {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> this.getBuilt(DupCommand.class, this.getOrder(reversed)));
        assertTrue(thrown.getMessage().contains("'dup same'"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("DupCommand#first"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("DupCommand#second"), thrown.getMessage());
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void samePathSamePermissionStillBuilds(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(TwinCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of("test.same"), "same");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void samePathSamePermissionDifferentSenderBuilds(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(MirrorCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of("test.mirror"), "same");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void consoleRunsTheMethodOwningEachPath(boolean reversed) {
        Comparator<Method> order = this.getOrder(reversed);
        assertAll(
                () -> assertEquals("balance", this.getExecutorOf(WalletCommand.class, order, "wallet balance"), "wallet balance"),
                () -> assertEquals("send", this.getExecutorOf(PayCommand.class, order, "pay send p 5"), "pay send p 5"),
                () -> assertEquals("sendWithNote", this.getExecutorOf(PayCommand.class, order, "pay send p 5 hello"), "pay send p 5 hello"),
                () -> assertEquals(reversed ? "first" : "second", this.getExecutorOf(TwinCommand.class, order, "twin same"), "twin same")
        );
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void literalAlternativesDoNotShareChildren(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(PickCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of(), "a", "amount");
        this.assertNode(root, true, true, List.of("test.pick.more"), "a", "amount", "extra");
        this.assertNode(root, true, true, List.of(), "b", "amount");
        assertNull(this.getNode(root, "b", "amount").getChild("extra"), "child 'extra' under 'pick b amount'");
    }

    @Test
    void noArgumentMethodGatesTheClassLiteral() throws Exception {
        CommandAnnotationHandler.CommandNode node = this.getCommandNode(MenuCommand.class, BY_NAME);
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(node).get(0);
        assertAll(
                () -> assertTrue(this.hasCommand(root), "command at 'menu'"),
                () -> assertTrue(this.isOpen(root), "open at 'menu'"),
                () -> assertEquals(List.of("test.menu"), this.getGateOf(root), "gate at 'menu'"),
                () -> assertTrue(node.getPermissions().stream().anyMatch(permission -> permission.value().equals("test.menu")), "permission test.menu registered")
        );
    }

    @Test
    void subCommandNameClashThrows() {
        assertAll(
                () -> this.assertClash(ClashCommand.class, "ClashGiveCommand", "ClashCommand#give"),
                () -> this.assertClash(ClashOpenParentCommand.class, "ClashOpenCommand", "ClashOpenParentCommand#give"),
                () -> this.assertClash(ClashTwinCommand.class, "HubInfoCommand", "ClashInfoCommand")
        );
    }

    @Test
    void classGateLetsNonPlayersThrough() throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(TradeCommand.class, BY_NAME).get(0);
        this.assertNode(root, false, true, List.of("test.trade"));
    }

    @Test
    void mixedClassGateLetsConsoleThrough() throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(BankersCommand.class, BY_NAME).get(0);
        this.assertNode(root, false, true, List.of("test.bankers"));
    }

    @Test
    void parentGateOverSubCommandLetsConsoleThrough() throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(HubCommand.class, BY_NAME).get(0);
        this.assertNode(root, false, true, List.of("test.hub"));
        this.assertNode(root, true, true, List.of(), "info");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void classAndRootMethodGatesCompose(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> root = this.getBuilt(VaultCommand.class, this.getOrder(reversed)).get(0);
        this.assertNode(root, true, true, List.of("test.vault", "test.vault.open"));
        this.assertNode(root, true, true, List.of(), "reload");
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void rootMethodGateAloneLetsConsoleThrough(boolean reversed) throws Exception {
        LiteralCommandNode<CommandSourceStack> locker = this.getBuilt(LockerCommand.class, this.getOrder(reversed)).get(0);
        LiteralCommandNode<CommandSourceStack> cabinet = this.getBuilt(CabinetCommand.class, BY_NAME).get(0);
        assertAll(
                () -> assertEquals(List.of("test.locker.open"), this.getGateOf(locker), "gate at 'locker'"),
                () -> assertTrue(this.isOpen(locker), "open at 'locker'"),
                () -> assertEquals(List.of("test.cabinet.open"), this.getGateOf(cabinet), "gate at 'cabinet'"),
                () -> assertTrue(this.isOpen(cabinet), "open at 'cabinet'")
        );
    }

    @ParameterizedTest(name = "{displayName} reversed = {0}")
    @ValueSource(booleans = {false, true})
    void twoRootMethodsWithDifferentGatesThrow(boolean reversed) {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> this.getBuilt(TwinRootCommand.class, this.getOrder(reversed)));
        assertTrue(thrown.getMessage().contains("'twin'"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("TwinRootCommand#first"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("TwinRootCommand#second"), thrown.getMessage());
    }

    private Comparator<Method> getOrder(boolean reversed) {
        return reversed ? BY_NAME.reversed() : BY_NAME;
    }

    private Comparator<Method> getNamedOrder(List<String> names) {
        return Comparator.comparingInt(method -> names.indexOf(method.getName()));
    }

    private CommandAnnotationHandler.CommandNode getCommandNode(Class<?> commandClass, Comparator<Method> order) throws Exception {
        CommandAnnotationHandler.executorOrder = order;
        Command command = commandClass.getAnnotation(Command.class);
        Object instance = commandClass.getDeclaredConstructor().newInstance();
        List<Method> executors = CommandAnnotationHandler.getExecutorsOf(commandClass);
        assertEquals(executors.stream().sorted(order).map(Method::getName).toList(), executors.stream().map(Method::getName).toList(), "executor order of " + commandClass.getSimpleName());
        return new CommandAnnotationHandler.CommandNode(commandClass, instance, command.value(), executors, command.subCommands());
    }

    private List<LiteralCommandNode<CommandSourceStack>> getBuilt(CommandAnnotationHandler.CommandNode node) throws Exception {
        List<LiteralCommandNode<CommandSourceStack>> built = new ArrayList<>();
        for (LiteralArgumentBuilder<CommandSourceStack> builder : node.build(REGISTRY)) built.add(builder.build());
        return built;
    }

    private List<LiteralCommandNode<CommandSourceStack>> getBuilt(Class<?> commandClass, Comparator<Method> order) throws Exception {
        return this.getBuilt(this.getCommandNode(commandClass, order));
    }

    private @Nullable String getExecutorOf(Class<?> commandClass, Comparator<Method> order, String input) throws Exception {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        for (LiteralCommandNode<CommandSourceStack> root : this.getBuilt(commandClass, order)) dispatcher.getRoot().addChild(root);
        CommandTreeTest.executed = null;
        assertEquals(1, dispatcher.execute(input, CONSOLE), "result of '" + input + "'");
        return CommandTreeTest.executed;
    }

    private CommandNode<CommandSourceStack> getNode(CommandNode<CommandSourceStack> root, String... path) {
        CommandNode<CommandSourceStack> node = root;
        for (String name : path) {
            node = node.getChild(name);
            assertNotNull(node, "missing node '" + name + "' in '" + root.getName() + " " + String.join(" ", path) + "'");
        }
        return node;
    }

    private boolean isOpen(CommandNode<CommandSourceStack> node) {
        return node.getRequirement().test(CONSOLE);
    }

    private boolean hasCommand(CommandNode<CommandSourceStack> node) {
        return node.getCommand() != null;
    }

    private List<String> getGateOf(CommandNode<CommandSourceStack> node) {
        return node.getRequirement() instanceof PermissionRequirement requirement ? requirement.getPermissions().stream().map(Permission::value).toList() : List.of();
    }

    private void assertNode(CommandNode<CommandSourceStack> root, boolean command, boolean open, List<String> gate, String... path) {
        CommandNode<CommandSourceStack> node = this.getNode(root, path);
        String name = (root.getName() + " " + String.join(" ", path)).trim();
        assertAll(
                () -> assertEquals(command, this.hasCommand(node), "command at '" + name + "'"),
                () -> assertEquals(open, this.isOpen(node), "open at '" + name + "'"),
                () -> assertEquals(gate, this.getGateOf(node), "gate at '" + name + "'")
        );
    }

    private void assertClash(Class<?> commandClass, String... names) {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> this.getBuilt(commandClass, BY_NAME), commandClass.getSimpleName());
        for (String name : names) assertTrue(thrown.getMessage().contains(name), thrown.getMessage());
    }

    private Map<String, String> getGateMap(List<LiteralCommandNode<CommandSourceStack>> roots) {
        Map<String, String> gates = new TreeMap<>();
        for (LiteralCommandNode<CommandSourceStack> root : roots) this.collectGates(gates, root.getName(), root);
        return gates;
    }

    private void collectGates(Map<String, String> gates, String path, CommandNode<CommandSourceStack> node) {
        gates.put(path, (this.isOpen(node) ? "open" : "gated") + (this.hasCommand(node) ? " command " : " ") + this.getGateOf(node));
        for (CommandNode<CommandSourceStack> child : node.getChildren()) this.collectGates(gates, path + " " + child.getName(), child);
    }

    @Command("eco")
    public static class EcoCommand {
        @Execute
        @Permission(value = "test.eco.give", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean give(@SenderArg ServerPlayer sender, @LiteralArg("give") String give, @TextArg(name = "player") String player, @IntegerArg("amount") int amount) {
            return true;
        }

        @Execute
        public boolean giveWithCurrency(@SenderArg CommandSourceStack sender, @LiteralArg("give") String give, @TextArg(name = "player") String player, @IntegerArg("amount") int amount, @TextArg(name = "currency") String currency) {
            return true;
        }
    }

    @Command("pay")
    public static class PayCommand {
        @Execute
        public boolean send(@SenderArg CommandSourceStack sender, @LiteralArg("send") String send, @TextArg(name = "player") String player, @IntegerArg("amount") int amount) {
            CommandTreeTest.executed = "send";
            return true;
        }

        @Execute
        @Permission(value = "test.pay.note", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean sendWithNote(@SenderArg CommandSourceStack sender, @LiteralArg("send") String send, @TextArg(name = "player") String player, @IntegerArg("amount") int amount, @TextArg(name = "note") String note) {
            CommandTreeTest.executed = "sendWithNote";
            return true;
        }
    }

    @Command("bank")
    public static class BankCommand {
        @Execute
        @Permission(value = "test.bank.deposit", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean deposit(@SenderArg ServerPlayer sender, @LiteralArg("deposit") String deposit, @IntegerArg("amount") int amount) {
            return true;
        }

        @Execute
        public boolean depositNote(@SenderArg CommandSourceStack sender, @LiteralArg("deposit") String deposit, @IntegerArg("amount") int amount, @TextArg(name = "note") String note) {
            return true;
        }

        @Execute
        @Permission(value = "test.bank.tag", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean depositTag(@SenderArg ServerPlayer sender, @LiteralArg("deposit") String deposit, @IntegerArg("amount") int amount, @TextArg(name = "note") String note, @TextArg(name = "tag") String tag) {
            return true;
        }
    }

    @Command({"shop", "store"})
    @Permission(value = "test.shop", permissionLevel = PermissionLevel.PLAYER)
    public static class ShopCommand {
        @Execute
        @Permission(value = "test.shop.sell", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean sell(@SenderArg ServerPlayer sender, @LiteralArg("sell") String sell, @TextArg(name = "item") String item, @IntegerArg("amount") int amount) {
            return true;
        }

        @Execute
        public boolean sellWithPrice(@SenderArg CommandSourceStack sender, @LiteralArg("sell") String sell, @TextArg(name = "item") String item, @IntegerArg("amount") int amount, @IntegerArg("price") int price) {
            return true;
        }
    }

    @Command(value = "admin", subCommands = AdminEcoCommand.class)
    public static class AdminCommand {
    }

    @Command("eco")
    @Permission(value = "test.admin.eco", permissionLevel = PermissionLevel.GAMEMASTER)
    public static class AdminEcoCommand {
        @Execute
        @Permission(value = "test.admin.eco.take", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean take(@SenderArg ServerPlayer sender, @LiteralArg("take") String take, @TextArg(name = "player") String player, @IntegerArg("amount") int amount) {
            return true;
        }

        @Execute
        public boolean takeWithReason(@SenderArg CommandSourceStack sender, @LiteralArg("take") String take, @TextArg(name = "player") String player, @IntegerArg("amount") int amount, @TextArg(name = "reason") String reason) {
            return true;
        }
    }

    @Command("wallet")
    public static class WalletCommand {
        @Execute
        public boolean balance(@SenderArg CommandSourceStack sender, @LiteralArg("balance") String balance) {
            CommandTreeTest.executed = "balance";
            return true;
        }

        @Execute
        @Permission(value = "test.wallet.others", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean balanceOf(@SenderArg ServerPlayer sender, @LiteralArg("balance") String balance, @TextArg(name = "player") String player) {
            return true;
        }
    }

    @Command("dup")
    public static class DupCommand {
        @Execute
        @Permission(value = "test.dup.a", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean first(@SenderArg ServerPlayer sender, @LiteralArg("same") String same) {
            return true;
        }

        @Execute
        @Permission(value = "test.dup.b", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean second(@SenderArg ServerPlayer sender, @LiteralArg("same") String same) {
            return true;
        }
    }

    @Command("twin")
    public static class TwinCommand {
        @Execute
        @Permission(value = "test.same", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean first(@SenderArg CommandSourceStack sender, @LiteralArg("same") String same) {
            CommandTreeTest.executed = "first";
            return true;
        }

        @Execute
        @Permission(value = "test.same", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean second(@SenderArg CommandSourceStack sender, @LiteralArg("same") String same) {
            CommandTreeTest.executed = "second";
            return true;
        }
    }

    @Command("mirror")
    public static class MirrorCommand {
        @Execute
        @Permission(value = "test.mirror", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean mine(@SenderArg ServerPlayer sender, @LiteralArg("same") String same) {
            return true;
        }

        @Execute
        @Permission(value = "test.mirror", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean theirs(@SenderArg CommandSourceStack sender, @LiteralArg("same") String same) {
            return true;
        }
    }

    @Command("pick")
    public static class PickCommand {
        @Execute
        public boolean choose(@SenderArg CommandSourceStack sender, @LiteralArg({"a", "b"}) String which, @IntegerArg("amount") int amount) {
            return true;
        }

        @Execute
        @Permission(value = "test.pick.more", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean chooseMore(@SenderArg ServerPlayer sender, @LiteralArg("a") String which, @IntegerArg("amount") int amount, @IntegerArg("extra") int extra) {
            return true;
        }
    }

    @Command("menu")
    public static class MenuCommand {
        @Execute
        @Permission(value = "test.menu", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean open(@SenderArg ServerPlayer sender) {
            return true;
        }
    }

    @Command(value = "clash", subCommands = ClashGiveCommand.class)
    public static class ClashCommand {
        @Execute
        public boolean give(@SenderArg CommandSourceStack sender, @LiteralArg("give") String give, @IntegerArg("amount") int amount) {
            return true;
        }
    }

    @Command("give")
    @Permission(value = "test.clash.give", permissionLevel = PermissionLevel.GAMEMASTER)
    public static class ClashGiveCommand {
        @Execute
        public boolean run(@SenderArg ServerPlayer sender) {
            return true;
        }
    }

    @Command(value = "clashopen", subCommands = ClashOpenCommand.class)
    public static class ClashOpenParentCommand {
        @Execute
        public boolean give(@SenderArg CommandSourceStack sender, @LiteralArg("give") String give, @IntegerArg("amount") int amount) {
            return true;
        }
    }

    @Command("give")
    public static class ClashOpenCommand {
        @Execute
        public boolean run(@SenderArg CommandSourceStack sender) {
            return true;
        }
    }

    @Command(value = "clashtwin", subCommands = {HubInfoCommand.class, ClashInfoCommand.class})
    public static class ClashTwinCommand {
    }

    @Command("info")
    public static class ClashInfoCommand {
        @Execute
        public boolean show(@SenderArg CommandSourceStack sender) {
            return true;
        }
    }

    @Command("trade")
    @Permission(value = "test.trade", permissionLevel = PermissionLevel.PLAYER)
    public static class TradeCommand {
        @Execute
        public boolean offer(@SenderArg ServerPlayer sender, @LiteralArg("offer") String offer, @TextArg(name = "item") String item) {
            return true;
        }

        @Execute
        public boolean accept(@SenderArg ServerPlayer sender, @LiteralArg("accept") String accept) {
            return true;
        }
    }

    @Command("bankers")
    @Permission(value = "test.bankers", permissionLevel = PermissionLevel.PLAYER)
    public static class BankersCommand {
        @Execute
        public boolean audit(@SenderArg CommandSourceStack sender, @LiteralArg("audit") String audit) {
            return true;
        }

        @Execute
        public boolean withdraw(@SenderArg ServerPlayer sender, @LiteralArg("withdraw") String withdraw) {
            return true;
        }
    }

    @Command(value = "hub", subCommands = HubInfoCommand.class)
    @Permission(value = "test.hub", permissionLevel = PermissionLevel.PLAYER)
    public static class HubCommand {
    }

    @Command("info")
    public static class HubInfoCommand {
        @Execute
        public boolean show(@SenderArg CommandSourceStack sender) {
            return true;
        }
    }

    @Command("vault")
    @Permission(value = "test.vault", permissionLevel = PermissionLevel.PLAYER)
    public static class VaultCommand {
        @Execute
        @Permission(value = "test.vault.open", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean open(@SenderArg ServerPlayer sender) {
            return true;
        }

        @Execute
        public boolean reload(@SenderArg CommandSourceStack sender, @LiteralArg("reload") String reload) {
            return true;
        }
    }

    @Command("locker")
    public static class LockerCommand {
        @Execute
        @Permission(value = "test.locker.open", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean open(@SenderArg ServerPlayer sender) {
            return true;
        }

        @Execute
        public boolean status(@SenderArg CommandSourceStack sender, @LiteralArg("status") String status) {
            return true;
        }
    }

    @Command("cabinet")
    public static class CabinetCommand {
        @Execute
        @Permission(value = "test.cabinet.open", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean open(@SenderArg ServerPlayer sender) {
            return true;
        }
    }

    @Command("twin")
    public static class TwinRootCommand {
        @Execute
        @Permission(value = "test.twin.a", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean first(@SenderArg ServerPlayer sender) {
            return true;
        }

        @Execute
        @Permission(value = "test.twin.b", permissionLevel = PermissionLevel.GAMEMASTER)
        public boolean second(@SenderArg ServerPlayer sender) {
            return true;
        }
    }
}
