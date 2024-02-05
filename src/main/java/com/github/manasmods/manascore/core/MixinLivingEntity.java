package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "getFrictionInfluencedSpeed", at = @At(value = "RETURN"), cancellable = true)
    private void additionSprintingSpeed(float friction, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.isOnGround() || !entity.isSprinting()) return;

        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.SPRINTING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        cir.setReturnValue((float) (cir.getReturnValue() * instance.getValue()));
    }
}
