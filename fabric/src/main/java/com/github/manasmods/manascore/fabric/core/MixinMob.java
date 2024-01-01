package com.github.manasmods.manascore.fabric.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(Mob.class)
public class MixinMob {
    @WrapOperation(method = "setTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Mob;target:Lnet/minecraft/world/entity/LivingEntity;"))
    private LivingEntity onSetTarget(Mob instance, LivingEntity value, Operation<LivingEntity> original) {
        Changeable<LivingEntity> target = Changeable.of(value);
        // Do nothing if the event is canceled
        if (EntityEvents.LIVING_CHANGE_TARGET.invoker().changeTarget(instance, target).isFalse()) return instance.getTarget();
        // If the target has changed, return the new target
        if (target.hasChanged()) return target.get();
        // Otherwise, return the original value
        return value;
    }
}
