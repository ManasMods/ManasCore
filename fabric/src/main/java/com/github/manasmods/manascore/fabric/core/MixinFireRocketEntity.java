package com.github.manasmods.manascore.fabric.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.utils.Changeable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireRocketEntity extends Projectile {
    public MixinFireRocketEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
    @Unique
    @Nullable
    private ProjectileHitResult onHitEventResult = null;
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    void onHit(FireworkRocketEntity instance, HitResult result, Operation<Void> original) {
        Changeable<ProjectileHitResult> resultChangeable = Changeable.of(ProjectileHitResult.DEFAULT);
        EntityEvents.PROJECTILE_HIT.invoker().hit(result, instance, resultChangeable);
        this.onHitEventResult = resultChangeable.get();

        switch (this.onHitEventResult) {
            case DEFAULT, HIT -> {
                original.call(instance, result);
                this.onHitEventResult = null;
            }
            case HIT_NO_DAMAGE -> this.discard();
            case PASS -> {
                if (result.getType() != HitResult.Type.ENTITY) {
                    original.call(instance, result);
                    this.onHitEventResult = null;
                }
            }
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;hasImpulse:Z"))
    void onImpulseSet(FireworkRocketEntity instance, boolean value, Operation<Boolean> original) {
        if (this.onHitEventResult == null) original.call(instance, value);
        this.onHitEventResult = null;
    }
}
