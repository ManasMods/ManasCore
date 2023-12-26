package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageHolder;
import com.github.manasmods.manascore.api.storage.StorageType;
import com.github.manasmods.manascore.storage.CombinedStorage;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class MixinLevel implements StorageHolder, LevelAccessor {
    @Unique
    private CombinedStorage storage;


    @Override
    public @NotNull CompoundTag manasCore$getStorage() {
        return this.storage.toNBT();
    }

    @Nullable
    @Override
    public <T extends Storage> T manasCore$getStorage(StorageKey<T> storageKey) {
        return (T) this.storage.get(storageKey.id());
    }

    @Override
    public void manasCore$attachStorage(@NotNull ResourceLocation id, @NotNull Storage storage) {
        this.storage.add(id, storage);
    }

    @Override
    public @NotNull StorageType manasCore$getStorageType() {
        return StorageType.WORLD;
    }

    @Override
    public @NotNull CombinedStorage manasCore$getCombinedStorage() {
        return this.storage;
    }

    @Override
    public void manasCore$setCombinedStorage(@NotNull CombinedStorage storage) {
        this.storage = storage;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void initStorage(WritableLevelData levelData, ResourceKey dimension, RegistryAccess registryAccess, Holder dimensionTypeRegistration, Supplier profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.storage = new CombinedStorage(this);
        StorageManager.initialStorageFilling(this);
    }
}
