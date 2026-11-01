/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.internal;

import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;

public class PermissionCheck {
    public static boolean isAllowed(IPermissionSource source, Permission permission) {
        if (!source.isPlayer()) return true;
        int level = permission.permissionLevel().getLevel();
        if (source.hasLevel(level) && (!source.isOwnCommand() || !source.hasOwnLevel(level))) return true;
        if (PermissionCheck.isGranted(source, permission.value(), level)) return true;
        for (String node : permission.anyOf()) if (PermissionCheck.isGranted(source, node, level)) return true;
        return false;
    }

    private static boolean isGranted(IPermissionSource source, String node, int level) {
        Boolean value = source.getValue(node);
        if (value != null) return value;
        return source.hasLevel(level);
    }

    public static boolean isOwnCommand(CommandSourceStack stack) {
        return stack.getPlayer() != null && stack.source == stack.getPlayer();
    }
}
