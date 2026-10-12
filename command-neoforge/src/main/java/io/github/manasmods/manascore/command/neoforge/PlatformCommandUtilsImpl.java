/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.neoforge;

import io.github.manasmods.manascore.command.ManasCoreCommand;
import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlatformCommandUtilsImpl {
    public static final Map<String, PermissionNode<Boolean>> PERMISSIONS = new HashMap<>();

    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        if (commandSourceStack.hasPermission(permission.permissionLevel().getLevel())) return true;
        if (!commandSourceStack.isPlayer()) return true;
        ServerPlayer player = Objects.requireNonNull(commandSourceStack.getPlayer());
        if (PermissionAPI.getPermission(player, PERMISSIONS.get(permission.value()))) return true;
        for (String node : permission.anyOf()) {
            PermissionNode<Boolean> permissionNode = PERMISSIONS.get(node);
            if (permissionNode != null && PermissionAPI.getPermission(player, permissionNode)) return true;
        }
        return false;
    }

    public static void registerPermission(Permission permission) {
        registerPermission(permission.value(), permission.permissionLevel());
    }

    public static void registerPermission(String permissionId, Permission.PermissionLevel level) {
        if (PERMISSIONS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.info("Permission with id {} already exists. Skipped registering.", permissionId);
            return;
        }
        var modId = permissionId.substring(0, permissionId.indexOf('.'));
        var nodeName = permissionId.substring(permissionId.indexOf('.') + 1);
        PERMISSIONS.put(permissionId, new PermissionNode<>(modId, nodeName, PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
            if (player == null) return true;
            return player.hasPermissions(level.getLevel());
        }));
    }
}
