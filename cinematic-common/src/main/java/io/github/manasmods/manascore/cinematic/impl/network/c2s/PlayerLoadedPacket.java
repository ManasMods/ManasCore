/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.cinematic.ModuleConstants;
import io.github.manasmods.manascore.cinematic.api.CutsceneHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PlayerLoadedPacket() implements CustomPacketPayload {
    public static final Type<PlayerLoadedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "player_loaded"));
    public static final StreamCodec<FriendlyByteBuf, PlayerLoadedPacket> STREAM_CODEC = CustomPacketPayload.codec(PlayerLoadedPacket::encode, PlayerLoadedPacket::new);

    public PlayerLoadedPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player) CutsceneHelper.setLoaded(player);
        });
    }

    public Type<PlayerLoadedPacket> type() {
        return TYPE;
    }
}
