/*
 * Copyright (c) 2025. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.fabric.integrations;

import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.fabric.PlatformCommandUtilsImpl;
import me.lucko.fabric.api.permissions.v0.Options;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiFunction;

public class FabricPermissionsApiIntegration {
    public static BiFunction<CommandSourceStack, Permission, Boolean> PERMISSION_CHECK = (commandSourceStack, permission) -> {
        if (Permissions.check(commandSourceStack, permission.value(), permission.permissionLevel().getLevel())) return true;
        for (String node : permission.anyOf()) {
            if (Permissions.check(commandSourceStack, node, PlatformCommandUtilsImpl.getLevelOf(node, permission))) return true;
        }
        return false;
    };

    public static boolean hasPermission(ServerPlayer player, String node, Permission.PermissionLevel level) {
        return level == Permission.PermissionLevel.EXPLICIT ? Permissions.check(player, node) : Permissions.check(player, node, level.getLevel());
    }

    public static int getNumber(ServerPlayer player, String node, int fallback) {
        return Options.get(player, node, fallback, Integer::parseInt);
    }
}
