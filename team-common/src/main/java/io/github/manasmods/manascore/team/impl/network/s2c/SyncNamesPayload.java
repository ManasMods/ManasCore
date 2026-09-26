/*
 * Copyright (c) 2025-2026. ManasMods
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

import java.util.Map;
import java.util.UUID;

public record SyncNamesPayload(Map<UUID, String> names) implements CustomPacketPayload {
    public static final Type<SyncNamesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "sync_names"));
    public static final StreamCodec<FriendlyByteBuf, SyncNamesPayload> STREAM_CODEC = CustomPacketPayload.codec(SyncNamesPayload::encode, SyncNamesPayload::new);

    public SyncNamesPayload(FriendlyByteBuf buf) {
        this(buf.readMap(b -> b.readUUID(), FriendlyByteBuf::readUtf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.names, (b, id) -> b.writeUUID(id), FriendlyByteBuf::writeUtf);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<SyncNamesPayload> type() {
        return TYPE;
    }
}
