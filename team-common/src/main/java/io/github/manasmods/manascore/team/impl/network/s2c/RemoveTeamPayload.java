/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.team.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RemoveTeamPayload(UUID teamId) implements CustomPacketPayload {
    public static final Type<RemoveTeamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "remove_team"));
    public static final StreamCodec<FriendlyByteBuf, RemoveTeamPayload> STREAM_CODEC = CustomPacketPayload.codec(RemoveTeamPayload::encode, RemoveTeamPayload::new);

    public RemoveTeamPayload(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.teamId);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<RemoveTeamPayload> type() {
        return TYPE;
    }
}
