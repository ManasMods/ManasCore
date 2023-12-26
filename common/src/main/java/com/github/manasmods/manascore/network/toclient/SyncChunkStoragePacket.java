package com.github.manasmods.manascore.network.toclient;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Exclude;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.function.Supplier;

@Getter
@ToString
public class SyncChunkStoragePacket implements StorageSyncPacket {
    private final boolean update;
    private final ChunkPos chunkPos;
    @Exclude
    private final CompoundTag storageTag;

    public SyncChunkStoragePacket(boolean update, ChunkPos chunkPos, CompoundTag storageTag) {
        this.update = update;
        this.chunkPos = chunkPos;
        this.storageTag = storageTag;
    }

    public SyncChunkStoragePacket(FriendlyByteBuf buf) {
        this.update = buf.readBoolean();
        this.chunkPos = buf.readChunkPos();
        this.storageTag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(update);
        buf.writeChunkPos(chunkPos);
        buf.writeNbt(storageTag);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }
}
