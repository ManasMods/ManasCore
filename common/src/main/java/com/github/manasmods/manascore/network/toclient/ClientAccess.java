package com.github.manasmods.manascore.network.toclient;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.storage.CombinedStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

class ClientAccess {
    static void handle(SyncEntityStoragePacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(packet.getEntityId());
        if (entity == null) return;
        CompoundTag tag = packet.getStorageTag();
        if (packet.isUpdate()) {
            entity.manasCore$getCombinedStorage().handleUpdatePacket(tag);
        } else {
            entity.manasCore$setCombinedStorage(new CombinedStorage(entity, tag));
        }
        ManasCore.Logger.info("Handled packet for {}", entity);
    }
}
