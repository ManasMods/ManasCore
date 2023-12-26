package com.github.manasmods.manascore.network.toclient;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.storage.StorageHolder;
import com.github.manasmods.manascore.storage.CombinedStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

class ClientAccess {
    static void handle(SyncEntityStoragePacket packet) {
        Entity entity = getEntityFromId(packet.getEntityId());
        if (entity == null) {
            ManasCore.Logger.warn("Entity with id {} not found. SyncEntityStoragePacket gets ignored!", packet.getEntityId());
            return;
        }
        handleUpdatePacket(entity, packet);
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

    static void handle(SyncChunkStoragePacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            ManasCore.Logger.warn("Level is null. SyncChunkStoragePacket gets ignored!");
            return;
        }
        LevelChunk chunk = level.getChunk(packet.getChunkPos().x, packet.getChunkPos().z);
        handleUpdatePacket(chunk, packet);
    }

    static void handle(SyncWorldStoragePacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            ManasCore.Logger.warn("Level is null. SyncWorldStoragePacket gets ignored!");
            return;
        }
        handleUpdatePacket(level, packet);
    }

    static void handleUpdatePacket(StorageHolder holder, StorageSyncPacket packet) {
        if (packet.isUpdate()) {
            holder.manasCore$getCombinedStorage().handleUpdatePacket(packet.getStorageTag());
        } else {
            holder.manasCore$setCombinedStorage(new CombinedStorage(holder, packet.getStorageTag()));
        }
    }
}
