package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Shadow public abstract float getJumpBoostPower();

    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    protected void getJumpPower(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.JUMP_POWER.get());
        if (instance == null) return;

        double newJump = (cir.getReturnValue() - getJumpBoostPower()) / 0.42 * instance.getValue();
        cir.setReturnValue((float) (newJump + getJumpBoostPower()));
    }

    @ModifyArg(method = "causeFallDamage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z"), index = 0)
    public float causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.JUMP_POWER.get());
        if (instance == null) return fallDistance;

        float additionalJumpBlock = (float) ((instance.getValue() - 0.42) / 0.2);
        return fallDistance - additionalJumpBlock;
    }

    @Inject(method = "jumpInLiquid", at = @At("HEAD"), cancellable = true)
    protected void jumpInLiquid(TagKey<Fluid> fluidTag, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.SWIM_SPEED.get());
        if (instance == null) return;

        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.04 * instance.getValue(), 0.0));
        ci.cancel();
    }

    @Inject(method = "goDownInWater", at = @At("HEAD"), cancellable = true)
    protected void goDownInWater(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.SWIM_SPEED.get());
        if (instance == null) return;

        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, -0.04 * instance.getValue(), 0.0));
        ci.cancel();
    }

    @ModifyArg(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"), index = 0)
    public float travel(float speed) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.SWIM_SPEED.get());
        if (instance == null) return speed;
        return (float) (speed * instance.getValue());
    }
}
