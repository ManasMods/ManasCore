package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Inject(method = "getFlyingSpeed", at = @At(value = "RETURN"), cancellable = true)
    private void flyingSpeedMultiplier(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.FLYING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        cir.setReturnValue((float) (cir.getReturnValue() * instance.getValue()));
    }
}
