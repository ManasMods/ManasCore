package com.github.manasmods.manascore.core.injection;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.github.manasmods.manascore.storage.StorageManager.StorageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface StorageHolder {
    default CompoundTag manasCore$getStorage() {
        return new CompoundTag();
    }

    @Nullable
    default <T extends Storage> T manasCore$getStorage(StorageKey<T> storageKey) {
        return null;
    }

    default void manasCore$sync() {
    }

    default void manasCore$sync(ServerPlayer target) {
    }

    default void manasCore$attachStorage(ResourceLocation id, Storage storage){
    }

    default StorageType manasCore$getStorageType() {
        return null;
    }
}
