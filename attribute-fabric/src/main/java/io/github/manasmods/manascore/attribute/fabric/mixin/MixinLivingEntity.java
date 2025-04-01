/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import io.github.manasmods.manascore.attribute.api.AttributeEvents;
import io.github.manasmods.manascore.attribute.fabric.ManasCoreAttributeRegisterImpl;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributeUtils;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "createLivingAttributes", at = @At("RETURN"), cancellable = true)
    private static void createLivingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue();
        for (Holder<Attribute> holder : ManasCoreAttributeRegisterImpl.GENERIC_REGISTRY) builder.add(holder);
        cir.setReturnValue(builder);
    }

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
}
