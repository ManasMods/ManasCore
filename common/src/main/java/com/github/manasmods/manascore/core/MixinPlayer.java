package com.github.manasmods.manascore.core;

import com.github.manasmods.manascore.attribute.ManasCoreAttributeUtils;
import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", opcode = Opcodes.GETSTATIC))
    private double getAttackDamageWithCritChance(Player player, Attribute attribute) {
        AttributeInstance instance = player.getAttribute(ManasCoreAttributes.CRIT_CHANCE.get());
        if (instance == null || player.getRandom().nextInt(100) > instance.getValue())
            return player.getAttributeValue(attribute);

        boolean vanillaCrit = player.getAttackStrengthScale(0.5F) > 0.9F && player.fallDistance > 0.0F
                && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.isSprinting()
                && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger();
        if (vanillaCrit) return player.getAttributeValue(attribute);

        return player.getAttributeValue(attribute) * getCritMultiplier(1.5F);
    }

    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(floatValue = 1.5F))
    private float getCritMultiplier(float multiplier) {
        Player player = (Player) (Object) this;
        AttributeInstance instance = player.getAttribute(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        if (instance == null) return multiplier;
        return (float) instance.getValue();
    }

    @ModifyConstant(method = "attack(Lnet/minecraft/world/entity/Entity;)V", constant = @Constant(doubleValue = 9.0))
    private double getAttackRangeSquared(double attackRange) {
        double range = ManasCoreAttributeUtils.getAttackRange((Player) (Object) this);
        return range * range;
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    protected void getDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        AttributeInstance instance = entity.getAttribute(ManasCoreAttributes.MINING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        cir.setReturnValue((float) (cir.getReturnValue() * instance.getValue()));
    }
}
