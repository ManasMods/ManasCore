/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManasCoreAttributeHandler {

    @SubscribeEvent
    public static void modifyJumpPower(final LivingJumpEvent e) {
        if (e.getEntity().getAttribute(ManasCoreAttributes.JUMP_POWER.get()) == null) return;

        final LivingEntity entity = e.getEntity();
        final BlockPos entityPos = entity.blockPosition();
        //Calculation
        double baseJumpPower = entity.getAttribute(ManasCoreAttributes.JUMP_POWER.get()).getValue();
        float blockModifier0 = entity.level.getBlockState(entityPos).getBlock().getJumpFactor();
        float blockModifier1 = entity.level.getBlockState(new BlockPos(entityPos.getX(), entity.getBoundingBox().minY - 0.5000001D, entityPos.getZ())).getBlock().getJumpFactor();
        double blockModifier = (double) blockModifier0 == 1.0D ? blockModifier1 : blockModifier0;
        double jumpPower = baseJumpPower * blockModifier;
        final double verticalVelocity = jumpPower + entity.getJumpBoostPower();
        //Apply velocity
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(vec3.x, verticalVelocity, vec3.z);
        if (entity.isSprinting()) {
            float f = entity.getYRot() * ((float) Math.PI / 180F);
            entity.setDeltaMovement(entity.getDeltaMovement().add(-Mth.sin(f) * 0.2F, 0.0D, Mth.cos(f) * 0.2F));
        }
        entity.hasImpulse = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void modifyCrit(final CriticalHitEvent e) {
        double critChance = e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_CHANCE.get()) / 100; // convert to %
        RandomSource rand = e.getEntity().getRandom();

        // Exit if this hit isn't a crit
        if (rand.nextFloat() > critChance && !e.isVanillaCritical()) return;

        float critMultiplier = (float) e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        float critModifier = (e.getDamageModifier() - 1) * critMultiplier;

        e.setDamageModifier(critModifier);
        e.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void modifyArrowCrit(final EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide) return;
        if (!(e.getEntity() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentData().getBoolean("manascore.crit.calc.done")) return;
        if (!(arrow.getOwner() instanceof LivingEntity owner)) return;
        // Check if current arrow is a crit arrow
        if (!arrow.isCritArrow()) {
            double critChance = owner.getAttributeValue(ManasCoreAttributes.CRIT_CHANCE.get());
            RandomSource rand = owner.getRandom();
            arrow.setCritArrow(rand.nextDouble() <= critChance);
        }
        arrow.getPersistentData().putBoolean("manascore.crit.calc.done", true);
    }
}
