package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageHolder;
import com.github.manasmods.manascore.api.storage.StorageType;
import com.github.manasmods.manascore.storage.CombinedStorage;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.github.manasmods.manascore.utils.PlayerLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity implements StorageHolder {
    @Shadow
    private Level level;
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
        return StorageType.ENTITY;
    }

    @Override
    public @NotNull CombinedStorage manasCore$getCombinedStorage() {
        return this.storage;
    }

    @Override
    public void manasCore$setCombinedStorage(@NotNull CombinedStorage storage) {
        this.storage = storage;
    }

    @Override
    public Iterable<ServerPlayer> manasCore$getTrackingPlayers() {
        return PlayerLookup.trackingAndSelf((Entity) (Object) this);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void initStorage(EntityType entityType, Level level, CallbackInfo ci) {
        // Create empty storage
        this.storage = new CombinedStorage(this);
        // Fill storage with data
        StorageManager.initialStorageFilling(this);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = Shift.AFTER), cancellable = true)
    void saveStorage(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        compound.put("ManasCoreStorage", this.storage.toNBT());
        cir.setReturnValue(compound);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = Shift.AFTER))
    void loadStorage(CompoundTag compound, CallbackInfo ci) {
        this.storage.load(compound.getCompound("ManasCoreStorage"));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    void onTickSyncCheck(CallbackInfo ci) {
        if (this.level.isClientSide()) return;
        this.level.getProfiler().push("manasCoreSyncCheck");
        if (this.storage.isDirty()) StorageManager.syncTracking((Entity) (Object) this, true);
        this.level.getProfiler().pop();
    }
}
