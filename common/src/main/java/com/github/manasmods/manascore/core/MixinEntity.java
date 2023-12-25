package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.core.injection.StorageHolder;
import com.github.manasmods.manascore.storage.CombinedStorage;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.storage.StorageManager.StorageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
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
    public CompoundTag manasCore$getStorage() {
        return this.storage.toNBT();
    }

    @Override
    public void manasCore$sync() {
        StorageManager.syncTracking((Entity) (Object) this);
    }

    @Override
    public void manasCore$sync(ServerPlayer target) {
        StorageManager.syncTarget((Entity) (Object) this, target);
    }

    @Override
    public void manasCore$attachStorage(ResourceLocation id, Storage storage) {
        this.storage.add(id, storage);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void initStorage(EntityType entityType, Level level, CallbackInfo ci) {
        this.storage = new CombinedStorage(StorageType.ENTITY, this);
        StorageManager.constructEntityStorage((Entity) (Object) this);
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = Shift.AFTER), cancellable = true)
    void saveStorage(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        compound.put("ManasCoreStorage", this.storage.toNBT());
        cir.setReturnValue(compound);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", shift = Shift.AFTER))
    void loadStorage(CompoundTag compound, CallbackInfo ci) {
        this.storage = new CombinedStorage(this, compound.getCompound("ManasCoreStorage"));
    }
}
