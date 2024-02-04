package com.github.manasmods.manascore.fabric.core;

import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import com.github.manasmods.manascore.utils.Changeable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class MixinPlayer {
    @ModifyArg(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float getCritChanceDamage(float amount, @Local(ordinal = 1) float g, @Local(ordinal = 2) boolean bl3) {
        Player player = (Player) (Object) this;
        if (!bl3) {
            AttributeInstance instance = player.getAttribute(ManasCoreAttributes.CRIT_CHANCE.get());
            if (instance == null || player.getRandom().nextInt(100) > instance.getValue()) return amount;

            float beforeEnchant = amount - g;
            return beforeEnchant * getCritMultiplier(1.5F) + g;
        }
        return amount;
    }

    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5F))
    private float getCritMultiplier(float multiplier) {
        Player player = (Player) (Object) this;
        AttributeInstance instance = player.getAttribute(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        if (instance == null) return multiplier;
        return (float) instance.getValue();
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    protected void getDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.MINING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        cir.setReturnValue((float) (cir.getReturnValue() * instance.getValue()));
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", shift = Shift.BEFORE), argsOnly = true)
    float modifyDamage(float amount, @Local DamageSource damageSource) {
        Changeable<Float> changeable = Changeable.of(amount);
        if (EntityEvents.LIVING_HURT.invoker().hurt((LivingEntity) (Object) this, damageSource, changeable).isFalse()) return 0.0F;
        return changeable.get();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", shift = Shift.BEFORE), cancellable = true)
    void cancelActuallyHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        if (damageAmount <= 0F) ci.cancel();
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "LOAD", ordinal = 6), argsOnly = true)
    float modifyTotalDamage(float amount, @Local DamageSource damageSource) {
        Changeable<Float> changeable = Changeable.of(amount);
        if (EntityEvents.LIVING_DAMAGE.invoker().damage((LivingEntity) (Object) this, damageSource, changeable).isFalse()) return 0.0F;
        return changeable.get();
    }
}
