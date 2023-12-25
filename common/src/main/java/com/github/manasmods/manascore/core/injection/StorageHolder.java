package com.github.manasmods.manascore.core.injection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface StorageHolder<T> {
    default CompoundTag manasCore$getStorage() {
        return new CompoundTag();
    }

    default void manasCore$markStorageDirty() {
    }

    default void manasCore$sync() {
    }

    default void manasCore$sync(ServerPlayer target) {
    }
}
