/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.fabric;

import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;

public class PlatformCommandUtilsImpl {

    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        return ManasCoreCommandFabric.hasPermission.apply(commandSourceStack, permission);
    }

    public static void registerPermission(Permission permission) {
        // No need to register permissions on Fabric
    }
}
