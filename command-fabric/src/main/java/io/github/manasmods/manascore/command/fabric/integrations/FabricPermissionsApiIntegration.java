/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.fabric.integrations;

import io.github.manasmods.manascore.command.api.Permission;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.BiFunction;

public class FabricPermissionsApiIntegration {
    public static BiFunction<CommandSourceStack, Permission, Boolean> PERMISSION_CHECK = (commandSourceStack, permission) -> Permissions.check(commandSourceStack, permission.value());
}
