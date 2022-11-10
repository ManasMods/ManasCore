/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

@RequiredArgsConstructor
public class PermissionHelper {
    private final String modId;

    public boolean hasPermissionOrIsConsole(CommandSourceStack sourceStack, PermissionNode<Boolean> node) {
        try {
            return PermissionAPI.getPermission(sourceStack.getPlayerOrException(), node);
        } catch (CommandSyntaxException e) {
            return true;
        }
    }

    public boolean hasPermissonAndIsPlayer(CommandSourceStack sourceStack, PermissionNode<Boolean> node) {
        try {
            return PermissionAPI.getPermission(sourceStack.getPlayerOrException(), node);
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public PermissionNode<Boolean> createNode(String node, boolean allowConsole, int permissionLevel) {
        return new PermissionNode<>(modId, node, PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
            if (player == null) return allowConsole;
            return player.hasPermissions(permissionLevel);
        });
    }
}
