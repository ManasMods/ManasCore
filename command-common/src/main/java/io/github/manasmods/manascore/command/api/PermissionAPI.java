/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.command.api;

import io.github.manasmods.manascore.command.PlatformCommandUtils;
import net.minecraft.world.entity.player.Player;

public class PermissionAPI {
    /**
     * Used to register a true or false permission node, like {@code manasrealms.exempt.tax}.
     * An operator at {@code level} holds the node even without a permissions mod.
     * On NeoForge such an operator always passes and a permissions mod cannot deny them. On Fabric a permissions mod can.
     * Use {@link Permission.PermissionLevel#EXPLICIT} for a node operators never hold implicitly.
     * <p>
     * Call it during mod init, before the server starts.
     * A node that is already registered, here or by a {@link Permission} annotation, keeps its first registration.
     * A node already registered with {@link #registerNumber} is skipped with a warning.
     *
     * @param node  the node, starting with the mod id, like {@code modid.some.node}
     * @param level the op level that holds the node without an explicit grant
     */
    public static void registerPermission(String node, Permission.PermissionLevel level) {
        if (node.indexOf('.') <= 0) throw new IllegalArgumentException("Permission node " + node + " has to start with a mod id, like modid.some.node");
        PlatformCommandUtils.registerPermission(node, level);
    }

    /**
     * Used to register a number permission node, like {@code manasrealms.claims.bonus}.
     * Admins set the number as meta, for example {@code /lp user <player> meta set manasrealms.claims.bonus 20}.
     * The op level never sets it.
     * <p>
     * Call it during mod init, before the server starts.
     * A node already registered as a true or false node is skipped with a warning.
     *
     * @param node         the node, starting with the mod id, like {@code modid.some.node}
     * @param defaultValue the number a player holds when nobody set one
     */
    public static void registerNumber(String node, int defaultValue) {
        if (node.indexOf('.') <= 0) throw new IllegalArgumentException("Permission node " + node + " has to start with a mod id, like modid.some.node");
        PlatformCommandUtils.registerNumber(node, defaultValue);
    }

    /**
     * Determine if the player holds the true or false {@code node}.
     * Returns false on the client and for a node that was never registered with {@link #registerPermission}
     * or a {@link Permission} annotation.
     */
    public static boolean hasPermission(Player player, String node) {
        return PlatformCommandUtils.hasPermission(player, node);
    }

    /**
     * This Method returns the number the player holds for {@code node}, or the registered default when nobody set one.
     * Returns the default on the client and 0 for a node that was never registered with {@link #registerNumber}.
     */
    public static int getNumber(Player player, String node) {
        return PlatformCommandUtils.getNumber(player, node);
    }
}
