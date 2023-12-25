package com.github.manasmods.manascore.core.injection;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.github.manasmods.manascore.storage.StorageManager.StorageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface StorageHolder {
    @NotNull
    default CompoundTag manasCore$getStorage() {
        return new CompoundTag();
    }

    @Nullable
    default <T extends Storage> T manasCore$getStorage(StorageKey<T> storageKey) {
        return null;
    }

    @NotNull
    default <T extends Storage> Optional<T> manasCore$getStorageOptional(StorageKey<T> storageKey) {
        return Optional.ofNullable(this.manasCore$getStorage(storageKey));
    }

    default void manasCore$sync() {
    }

    default void manasCore$sync(@NotNull ServerPlayer target) {
    }

    default void manasCore$attachStorage(@NotNull ResourceLocation id,@NotNull Storage storage){
    }

    @NotNull
    default StorageType manasCore$getStorageType() {
        throw new AssertionError();
    }
}
