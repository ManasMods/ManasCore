package com.github.manasmods.manascore.network.toclient;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import lombok.Getter;
import lombok.ToString;
import lombok.ToString.Exclude;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

@Getter
@ToString
public class SyncEntityStoragePacket {
    private final boolean update;
    private final int entityId;
    @Exclude
    private final CompoundTag storageTag;

    public SyncEntityStoragePacket(boolean update, int entityId, CompoundTag storageTag) {
        this.update = update;
        this.entityId = entityId;
        this.storageTag = storageTag;
    }

    public SyncEntityStoragePacket(FriendlyByteBuf buf) {
        this.update = buf.readBoolean();
        this.entityId = buf.readInt();
        this.storageTag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(update);
        buf.writeInt(entityId);
        buf.writeNbt(storageTag);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }
}
