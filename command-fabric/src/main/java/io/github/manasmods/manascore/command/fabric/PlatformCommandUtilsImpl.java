/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.fabric;

import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;

import java.util.HashMap;
import java.util.Map;

public class PlatformCommandUtilsImpl {
    public static final Map<String, Permission.PermissionLevel> PERMISSIONS = new HashMap<>();

    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        return ManasCoreCommandFabric.hasPermission.apply(commandSourceStack, permission);
    }

    public static int getLevelOf(String permissionId, Permission permission) {
        return PERMISSIONS.getOrDefault(permissionId, permission.permissionLevel()).getLevel();
    }

    public static void registerPermission(Permission permission) {
        registerPermission(permission.value(), permission.permissionLevel());
    }

    public static void registerPermission(String permissionId, Permission.PermissionLevel level) {
        PERMISSIONS.putIfAbsent(permissionId, level);
    }
}
