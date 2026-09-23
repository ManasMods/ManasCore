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

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SyncInvitablePayload(UUID teamId, List<UUID> players, Map<UUID, String> names) implements CustomPacketPayload {
    public static final Type<SyncInvitablePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "sync_invitable"));
    public static final StreamCodec<FriendlyByteBuf, SyncInvitablePayload> STREAM_CODEC = CustomPacketPayload.codec(SyncInvitablePayload::encode, SyncInvitablePayload::new);

    public SyncInvitablePayload(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readList(b -> b.readUUID()), buf.readMap(b -> b.readUUID(), b -> b.readUtf()));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.teamId);
        buf.writeCollection(this.players, (b, u) -> b.writeUUID(u));
        buf.writeMap(this.names, (b, id) -> b.writeUUID(id), (b, name) -> b.writeUtf(name));
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<SyncInvitablePayload> type() {
        return TYPE;
    }
}
