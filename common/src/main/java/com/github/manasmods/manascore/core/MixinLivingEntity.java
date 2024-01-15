package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = Shift.BEFORE))
    void onPreTick(CallbackInfo ci) {
        EntityEvents.LIVING_PRE_TICK.invoker().tick((LivingEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;refreshDirtyAttributes()V", shift = Shift.AFTER))
    void onPostTick(CallbackInfo ci) {
        EntityEvents.LIVING_POST_TICK.invoker().tick((LivingEntity) (Object) this);
    }
}
