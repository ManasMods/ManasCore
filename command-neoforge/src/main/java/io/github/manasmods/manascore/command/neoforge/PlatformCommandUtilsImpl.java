/*
 * Copyright (c) 2025. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.neoforge;

import io.github.manasmods.manascore.command.ManasCoreCommand;
import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.internal.IPermissionSource;
import io.github.manasmods.manascore.command.internal.PermissionCheck;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlatformCommandUtilsImpl {
    public static final Map<String, PermissionNode<Boolean>> PERMISSIONS = new HashMap<>();
    public static final Map<String, PermissionNode<Integer>> NUMBERS = new HashMap<>();
    public static boolean gathered = false;

    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        return PermissionCheck.isAllowed(new IPermissionSource() {
            public boolean isPlayer() {
                return commandSourceStack.isPlayer();
            }

            public boolean isOwnCommand() {
                return PermissionCheck.isOwnCommand(commandSourceStack);
            }

            public boolean hasLevel(int level) {
                return commandSourceStack.hasPermission(level);
            }

            public boolean hasOwnLevel(int level) {
                ServerPlayer player = commandSourceStack.getPlayer();
                return player != null && player.hasPermissions(level);
            }

            public @Nullable Boolean getValue(String node) {
                ServerPlayer player = commandSourceStack.getPlayer();
                PermissionNode<Boolean> permissionNode = PERMISSIONS.get(node);
                if (permissionNode == null || player == null) return null;
                if (!PermissionAPI.getRegisteredNodes().contains(permissionNode)) return permissionNode.getDefaultResolver().resolve(player, player.getUUID());
                return PermissionAPI.getPermission(player, permissionNode);
            }
        }, permission);
    }

    public static void registerPermission(Permission permission) {
        registerPermission(permission.value(), permission.permissionLevel());
    }

    public static synchronized void registerPermission(String permissionId, Permission.PermissionLevel level) {
        if (PERMISSIONS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.info("Permission with id {} already exists. Skipped registering.", permissionId);
            return;
        }
        if (NUMBERS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.warn("Permission {} is already registered as a number node. Skipped registering.", permissionId);
            return;
        }
        if (gathered) ManasCoreCommand.LOG.warn("Permission {} was registered after the server gathered permission nodes. It answers its default until the next server start.", permissionId);
        var modId = permissionId.substring(0, permissionId.indexOf('.'));
        var nodeName = permissionId.substring(permissionId.indexOf('.') + 1);
        PERMISSIONS.put(permissionId, new PermissionNode<>(modId, nodeName, PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
            if (level == Permission.PermissionLevel.EXPLICIT) return false;
            if (player == null) return true;
            return player.hasPermissions(level.getLevel());
        }));
    }

    public static synchronized void registerNumber(String permissionId, int defaultValue) {
        if (NUMBERS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.info("Permission with id {} already exists. Skipped registering.", permissionId);
            return;
        }
        if (PERMISSIONS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.warn("Permission {} is already registered as a true or false node. Skipped registering.", permissionId);
            return;
        }
        if (gathered) ManasCoreCommand.LOG.warn("Permission {} was registered after the server gathered permission nodes. It answers its default until the next server start.", permissionId);
        String modId = permissionId.substring(0, permissionId.indexOf('.'));
        String nodeName = permissionId.substring(permissionId.indexOf('.') + 1);
        NUMBERS.put(permissionId, new PermissionNode<>(modId, nodeName, PermissionTypes.INTEGER, (player, playerUUID, context) -> defaultValue));
    }

    public static boolean hasPermission(Player player, String permissionId) {
        PermissionNode<Boolean> node = PERMISSIONS.get(permissionId);
        if (node == null || !(player instanceof ServerPlayer serverPlayer)) return false;
        if (!PermissionAPI.getRegisteredNodes().contains(node)) return node.getDefaultResolver().resolve(serverPlayer, serverPlayer.getUUID());
        return Boolean.TRUE.equals(PermissionAPI.getPermission(serverPlayer, node));
    }

    public static int getNumber(Player player, String permissionId) {
        PermissionNode<Integer> node = NUMBERS.get(permissionId);
        if (node == null) return 0;
        int fallback = node.getDefaultResolver().resolve(null, Util.NIL_UUID);
        if (!(player instanceof ServerPlayer serverPlayer) || !PermissionAPI.getRegisteredNodes().contains(node)) return fallback;
        Integer value = PermissionAPI.getPermission(serverPlayer, node);
        return value == null ? fallback : value;
    }
}
