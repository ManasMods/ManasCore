package com.github.manasmods.manascore.core.injection;

import com.github.manasmods.manascore.api.storage.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface StorageHolder {
    default CompoundTag manasCore$getStorage() {
        return new CompoundTag();
    }

    default void manasCore$sync() {
    }

    default void manasCore$sync(ServerPlayer target) {
    }

    default void manasCore$attachStorage(ResourceLocation id, Storage storage){
    }
}
