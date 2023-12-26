package com.github.manasmods.manascore.api.storage;

import com.github.manasmods.manascore.storage.CombinedStorage;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface StorageHolder {
    @NotNull
    default CompoundTag manasCore$getStorage() {
        throw new AssertionError();
    }

    @Nullable
    default <T extends Storage> T manasCore$getStorage(StorageKey<T> storageKey) {
        throw new AssertionError();
    }

    @NotNull
    default <T extends Storage> Optional<T> manasCore$getStorageOptional(StorageKey<T> storageKey) {
        return Optional.ofNullable(this.manasCore$getStorage(storageKey));
    }

    default void manasCore$sync(boolean update) {
        StorageManager.syncTracking(this, update);
    }

    default void manasCore$sync() {
        this.manasCore$sync(false);
    }

    default void manasCore$sync(@NotNull ServerPlayer target) {
        StorageManager.syncTarget(this, target);
    }

    default void manasCore$attachStorage(@NotNull ResourceLocation id, @NotNull Storage storage) {
        throw new AssertionError();
    }

    @NotNull
    default StorageType manasCore$getStorageType() {
        throw new AssertionError();
    }

    @NotNull
    default CombinedStorage manasCore$getCombinedStorage() {
        throw new AssertionError();
    }

    default void manasCore$setCombinedStorage(@NotNull CombinedStorage storage) {
        throw new AssertionError();
    }

    default Iterable<ServerPlayer> manasCore$getTrackingPlayers() {
        throw new AssertionError();
    }
}
