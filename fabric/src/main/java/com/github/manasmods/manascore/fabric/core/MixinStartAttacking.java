package com.github.manasmods.manascore.fabric.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.utils.Changeable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public class MixinStartAttacking {
    @Inject(method = "method_47123", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V", shift = Shift.BEFORE), cancellable = true)
    private static <E extends Mob> void onCreate(Predicate<E> canAttack, Function<E, Optional<? extends LivingEntity>> targetFinder, MemoryAccessor memoryAccessor, MemoryAccessor memoryAccessor2, ServerLevel serverLevel, Mob mob, long l, CallbackInfoReturnable<Boolean> cir, @Local LocalRef<LivingEntity> target) {
        Changeable<LivingEntity> changeable = Changeable.of(target.get());
        // Do nothing if the event is canceled
        if (EntityEvents.LIVING_CHANGE_TARGET.invoker().changeTarget(mob, changeable).isFalse()) {
            cir.setReturnValue(false);
            return;
        }

        if (changeable.hasChanged()) {
            target.set(changeable.get());
        }
    }
}
