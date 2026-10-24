/*
 * Copyright (c) 2025. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.manasmods.manascore.command.api.Permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PlatformCommandUtils {
    @ExpectPlatform
    public static boolean hasPermission(CommandSourceStack commandSourceStack, Permission permission) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPermission(Permission permission) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerPermission(String permissionId, Permission.PermissionLevel level) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerNumber(String permissionId, int defaultValue) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasPermission(Player player, String permissionId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getNumber(Player player, String permissionId) {
        throw new AssertionError();
    }
}
