/*
 * Copyright (c) 2025. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.command.fabric;

import io.github.manasmods.manascore.command.ManasCoreCommand;
import io.github.manasmods.manascore.command.api.Permission;
import io.github.manasmods.manascore.command.fabric.integrations.FabricPermissionsApiIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlatformCommandUtilsImpl {
    private static final boolean HAS_PERMISSIONS_API = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
    public static final Map<String, Permission.PermissionLevel> PERMISSIONS = new HashMap<>();
    public static final Map<String, Integer> NUMBERS = new HashMap<>();

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
        if (NUMBERS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.warn("Permission {} is already registered as a number node. Skipped registering.", permissionId);
            return;
        }
        PERMISSIONS.putIfAbsent(permissionId, level);
    }

    public static void registerNumber(String permissionId, int defaultValue) {
        if (PERMISSIONS.containsKey(permissionId)) {
            ManasCoreCommand.LOG.warn("Permission {} is already registered as a true or false node. Skipped registering.", permissionId);
            return;
        }
        NUMBERS.putIfAbsent(permissionId, defaultValue);
    }

    public static boolean hasPermission(Player player, String permissionId) {
        Permission.PermissionLevel level = PERMISSIONS.get(permissionId);
        if (level == null || !(player instanceof ServerPlayer serverPlayer)) return false;
        if (HAS_PERMISSIONS_API) return FabricPermissionsApiIntegration.hasPermission(serverPlayer, permissionId, level);
        return level != Permission.PermissionLevel.EXPLICIT && serverPlayer.hasPermissions(level.getLevel());
    }

    public static int getNumber(Player player, String permissionId) {
        Integer fallback = NUMBERS.get(permissionId);
        if (fallback == null) return 0;
        if (!HAS_PERMISSIONS_API || !(player instanceof ServerPlayer serverPlayer)) return fallback;
        return FabricPermissionsApiIntegration.getNumber(serverPlayer, permissionId, fallback);
    }
}
