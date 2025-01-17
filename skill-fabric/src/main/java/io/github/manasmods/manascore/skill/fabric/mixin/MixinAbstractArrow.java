/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.manasmods.manascore.skill.utils.Changeable;
import io.github.manasmods.manascore.skill.utils.EntityEvents;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow extends Projectile {
    public MixinAbstractArrow(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract void setPierceLevel(byte pierceLevel);

    @Unique
    private EntityEvents.ProjectileHitResult onHitEventResult = EntityEvents.ProjectileHitResult.DEFAULT;
    @Unique
    private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    ProjectileDeflection onHit(AbstractArrow instance, HitResult result, Operation<ProjectileDeflection> original, @Local LocalRef<EntityHitResult> entityHitResult) {
        Changeable<EntityEvents.ProjectileHitResult> resultChangeable = Changeable.of(EntityEvents.ProjectileHitResult.DEFAULT);
        Changeable<ProjectileDeflection> deflectionChangeable = Changeable.of(ProjectileDeflection.NONE);
        EntityEvents.PROJECTILE_HIT.invoker().hit(result, instance, deflectionChangeable, resultChangeable);
        this.onHitEventResult = resultChangeable.get();
        if (this.onHitEventResult == null) return original.call(instance, result);

        switch (this.onHitEventResult) {
            case DEFAULT -> {
                original.call(instance, result);
                this.onHitEventResult = null;
                return deflectionChangeable.get();
            }
            case HIT -> {
                this.setPierceLevel((byte) 0);
                original.call(instance, result);
                this.onHitEventResult = null;
                return deflectionChangeable.get();
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
        return deflectionChangeable.get();
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
