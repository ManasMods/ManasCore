package com.github.manasmods.manascore.network.toclient;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.storage.CombinedStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

class ClientAccess {
    static void handle(SyncEntityStoragePacket packet) {
        Entity entity = getEntityFromId(packet.getEntityId());
        if (entity == null) {
            ManasCore.Logger.warn("Entity with id {} not found. SyncEntityStoragePacket gets ignored!", packet.getEntityId());
            return;
        }
        CompoundTag tag = packet.getStorageTag();
        if (packet.isUpdate()) {
            entity.manasCore$getCombinedStorage().handleUpdatePacket(tag);
        } else {
            entity.manasCore$setCombinedStorage(new CombinedStorage(entity, tag));
        }
    }

    @Nullable
    static Entity getEntityFromId(int id) {
        // Early return if this is a self-update packet
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == id) return Minecraft.getInstance().player;
        // Get entity from level
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;
        return level.getEntity(id);
    }
}
