/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.team.ModuleConstants;
import io.github.manasmods.manascore.team.api.TeamInvite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SyncInvitesPayload(List<TeamInvite> incoming, List<TeamInvite> outgoing, Map<UUID, String> names) implements CustomPacketPayload {
    public static final Type<SyncInvitesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "sync_invites"));
    public static final StreamCodec<FriendlyByteBuf, SyncInvitesPayload> STREAM_CODEC = CustomPacketPayload.codec(SyncInvitesPayload::encode, SyncInvitesPayload::new);

    public SyncInvitesPayload(FriendlyByteBuf buf) {
        this(buf.readList(TeamInvite::read), buf.readList(TeamInvite::read), buf.readMap(b -> b.readUUID(), FriendlyByteBuf::readUtf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.incoming, (b, invite) -> invite.write(b));
        buf.writeCollection(this.outgoing, (b, invite) -> invite.write(b));
        buf.writeMap(this.names, (b, id) -> b.writeUUID(id), FriendlyByteBuf::writeUtf);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<SyncInvitesPayload> type() {
        return TYPE;
    }
}
