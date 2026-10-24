/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.cinematic.impl.network.s2c.DialoguePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class DialogueHelper {
    public static void send(Player player, Component speaker, Component line) {
        send(player, speaker, line, false);
    }

    public static void sendInstant(Player player, Component line) {
        send(player, Component.empty(), line, true);
    }

    public static void sendInstant(Player player, Component speaker, Component line) {
        send(player, speaker, line, true);
    }

    private static void send(Player player, Component speaker, Component line, boolean instant) {
        if (player instanceof ServerPlayer serverPlayer) NetworkManager.sendToPlayer(serverPlayer, new DialoguePayload(speaker, line, instant));
    }
}
