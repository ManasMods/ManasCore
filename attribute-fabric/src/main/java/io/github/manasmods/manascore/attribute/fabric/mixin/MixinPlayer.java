/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import io.github.manasmods.manascore.attribute.api.AttributeEvents;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributeUtils;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "HEAD"))
    void applyCriticalDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalFloatRef newAmount) {
        if (damageSource.getDirectEntity() instanceof LivingEntity attacker) { // Direct attack
            if (attacker instanceof Player) return; // Players have their own Critical Event
            LivingEntity target = (LivingEntity) (Object) this;

            Changeable<Float> multiplier = Changeable.of((float) attacker.getAttributeValue(ManasCoreAttributes.CRITICAL_DAMAGE_MULTIPLIER));
            Changeable<Double> chance = Changeable.of(attacker.getAttributeValue(ManasCoreAttributes.CRITICAL_ATTACK_CHANCE) / 100);
            if (AttributeEvents.CRITICAL_ATTACK_CHANCE_EVENT.invoker().applyCrit(attacker, target, 1, multiplier, chance).isFalse()) return;

            if (target.getRandom().nextFloat() > chance.get()) return;
            ManasCoreAttributeUtils.triggerCriticalAttackEffect(target, attacker);
            newAmount.set(amount * multiplier.get());
        }
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float getCritChanceDamage(float amount, @Local(ordinal = 0, argsOnly = true) Entity target,
                                      @Local(ordinal = 1) float enchantDamage, @Local(ordinal = 2) boolean vanillaCrit) {
        Player player = (Player) (Object) this;
        if (!vanillaCrit) {
            Changeable<Float> multiplier = Changeable.of((float) player.getAttributeValue(ManasCoreAttributes.CRITICAL_DAMAGE_MULTIPLIER));
            Changeable<Double> chance = Changeable.of(player.getAttributeValue(ManasCoreAttributes.CRITICAL_ATTACK_CHANCE) / 100);
            if (AttributeEvents.CRITICAL_ATTACK_CHANCE_EVENT.invoker().applyCrit(player, target, 1, multiplier, chance).isFalse()) return amount;

            if (player.getRandom().nextFloat() > chance.get()) return amount;
            ManasCoreAttributeUtils.triggerCriticalAttackEffect(target, player);
            return (amount - enchantDamage) * multiplier.get() + enchantDamage;
        }
        return amount;
    }

    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5F))
    private float getCritMultiplier(float multiplier) {
        return (float) ((Player) (Object) this).getAttributeValue(ManasCoreAttributes.CRITICAL_DAMAGE_MULTIPLIER);
    }
}
