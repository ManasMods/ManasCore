/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.team.ModuleConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncTeamPayload(ResourceLocation typeId, CompoundTag teamTag) implements CustomPacketPayload {
    public static final Type<SyncTeamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "sync_team"));
    public static final StreamCodec<FriendlyByteBuf, SyncTeamPayload> STREAM_CODEC = CustomPacketPayload.codec(SyncTeamPayload::encode, SyncTeamPayload::new);

    public SyncTeamPayload(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.typeId);
        buf.writeNbt(this.teamTag);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<SyncTeamPayload> type() {
        return TYPE;
    }
}
