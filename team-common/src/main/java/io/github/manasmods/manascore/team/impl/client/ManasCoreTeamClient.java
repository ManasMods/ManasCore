/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import io.github.manasmods.manascore.team.api.MemberInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;

public class ManasCoreTeamClient {
    private ManasCoreTeamClient() {
    }

    public static void init() {
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> ClientTeamCache.clear());
        ClientTeamCache.setOnlineResolver(ManasCoreTeamClient::resolveOnline);
        ClientTickEvent.CLIENT_POST.register(client -> ClientTeamCache.drainPending());
    }

    private static Optional<MemberInfo> resolveOnline(UUID id) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return Optional.empty();
        PlayerInfo info = connection.getPlayerInfo(id);
        if (info == null) return Optional.empty();
        return Optional.of(new MemberInfo(id, Component.literal(info.getProfile().getName()), true));
    }
}
