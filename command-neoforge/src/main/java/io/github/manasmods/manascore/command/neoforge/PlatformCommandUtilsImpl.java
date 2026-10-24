/*
 * Copyright (c) 2025. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.neoforge;

import io.github.manasmods.manascore.command.ManasCoreCommand;
import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlatformCommandUtilsImpl {
    public static final Map<String, PermissionNode<Boolean>> PERMISSIONS = new HashMap<>();
    public static final Map<String, PermissionNode<Integer>> NUMBERS = new HashMap<>();
    public static boolean gathered = false;

    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        if (commandSourceStack.hasPermission(permission.permissionLevel().getLevel())) return true;
        if (!commandSourceStack.isPlayer()) return true;
        ServerPlayer player = Objects.requireNonNull(commandSourceStack.getPlayer());
        PermissionNode<Boolean> main = PERMISSIONS.get(permission.value());
        if (main != null && PermissionAPI.getPermission(player, main)) return true;
        for (String node : permission.anyOf()) {
            PermissionNode<Boolean> permissionNode = PERMISSIONS.get(node);
            if (permissionNode != null && PermissionAPI.getPermission(player, permissionNode)) return true;
        }
        return false;
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
        if (node.getDefaultResolver().resolve(serverPlayer, serverPlayer.getUUID())) return true;
        if (!PermissionAPI.getRegisteredNodes().contains(node)) return false;
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
