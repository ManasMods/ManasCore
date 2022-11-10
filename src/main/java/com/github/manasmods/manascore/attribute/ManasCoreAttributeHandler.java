/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManasCoreAttributeHandler {

    @SubscribeEvent
    public static void applyAttributesToEntities(final EntityAttributeModificationEvent e) {
        e.add(EntityType.PLAYER, ManasCoreAttributes.JUMP_POWER.get());
    }

    @SubscribeEvent
    public static void modifyJumpPower(final LivingEvent.LivingJumpEvent e) {
        if (e.getEntityLiving().getAttribute(ManasCoreAttributes.JUMP_POWER.get()) == null) return;

        final LivingEntity entity = e.getEntityLiving();
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
}
