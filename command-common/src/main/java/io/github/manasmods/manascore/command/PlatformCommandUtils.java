/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;

public class PlatformCommandUtils {
    @ExpectPlatform
    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPermission(Permission permission) {
        throw new AssertionError();
    }
}
