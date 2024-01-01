package com.github.manasmods.manascore.fabric.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents.ProjectileHitResult;
import com.github.manasmods.manascore.utils.Changeable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow extends Projectile {
    public MixinAbstractArrow(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract void setPierceLevel(byte pierceLevel);

    @Unique
    @Nullable
    private ProjectileHitResult onHitEventResult = null;
    private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    void onHit(AbstractArrow instance, HitResult result, Operation<Void> original, @Local LocalRef<EntityHitResult> entityHitResult) {
        Changeable<ProjectileHitResult> resultChangeable = Changeable.of(ProjectileHitResult.DEFUALT);
        EntityEvents.PROJECTILE_HIT.invoker().hit(result, instance, resultChangeable);
        this.onHitEventResult = resultChangeable.get();

        switch (this.onHitEventResult) {
            case DEFUALT -> {
                original.call(instance, result);
                this.onHitEventResult = null;
            }
            case HIT -> {
                this.setPierceLevel((byte) 0);
                original.call(instance, result);
                this.onHitEventResult = null;
            }
            case HIT_NO_DAMAGE -> {
                this.discard();
                entityHitResult.set(null);
            }
            case PASS -> {
                if (result.getType() != HitResult.Type.ENTITY) {
                    original.call(instance, result);
                    this.onHitEventResult = null;
                } else {
                    this.ignoredEntities.add(entityHitResult.get().getEntity().getId());
                    entityHitResult.set(null);
                }
            }
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hasImpulse:Z"))
    void onImpulseSet(AbstractArrow instance, boolean value, Operation<Boolean> original) {
        if (this.onHitEventResult == null) original.call(instance, value);
        this.onHitEventResult = null;
    }

    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    void ignoreEntities(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        cir.setReturnValue(!this.ignoredEntities.contains(target.getId()));
    }
}
