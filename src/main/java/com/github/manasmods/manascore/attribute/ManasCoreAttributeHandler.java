/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.attribute.event.ArrowCriticalChanceEvent;
import com.github.manasmods.manascore.api.attribute.event.CriticalChanceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
        AttributeInstance instance = e.getEntity().getAttribute(ManasCoreAttributes.JUMP_POWER.get());
        if (instance == null) return;

        final LivingEntity entity = e.getEntity();
        final BlockPos entityPos = entity.blockPosition();
        //Calculation
        double baseJumpPower = instance.getValue();
        float blockModifier0 = entity.level.getBlockState(entityPos).getBlock().getJumpFactor();
        float blockModifier1 = entity.level.getBlockState(new BlockPos(entityPos.getX(), entity.getBoundingBox().minY - 0.5000001D, entityPos.getZ())).getBlock().getJumpFactor();
        double blockModifier = (double) blockModifier0 == 1.0D ? blockModifier1 : blockModifier0;
        double jumpPower = baseJumpPower * blockModifier;
        final double verticalVelocity = jumpPower + entity.getJumpBoostPower();
        //Apply velocity
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(vec3.x, verticalVelocity, vec3.z);
        entity.hasImpulse = true;
    }

    @SubscribeEvent
    public static void modifyFallDamage(final LivingFallEvent e) {
        AttributeInstance instance = e.getEntity().getAttribute(ManasCoreAttributes.JUMP_POWER.get());
        if (instance == null) return;
        if (instance.getValue() == 0.42) return;

        double additionalJumpBlock = (instance.getValue() - 0.42) / 0.2;
        e.setDistance((float) (e.getDistance() - additionalJumpBlock));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void applyEntityCrit(final LivingHurtEvent e) {
        if (!(e.getSource().getDirectEntity() instanceof LivingEntity entity)) return; // Direct attack
        if (entity instanceof Player) return; // Players have their own Critical Event
        LivingEntity target = e.getEntity();

        double critChance = entity.getAttributeValue(ManasCoreAttributes.CRIT_CHANCE.get()) / 100; // Convert to %
        float critMultiplier = (float) entity.getAttributeValue(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        CriticalChanceEvent event = new CriticalChanceEvent(entity, target, critMultiplier, critChance);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        RandomSource random = entity.getRandom();
        if (random.nextFloat() > event.getCritChance()) return;
        e.setAmount(e.getAmount() * event.getDamageModifier());

        target.getLevel().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, entity.getSoundSource(), 1.0F, 1.0F);
        if (entity.getLevel() instanceof ServerLevel level)
            level.getChunkSource().broadcastAndSend(entity, new ClientboundAnimatePacket(target, 4));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void modifyCrit(final CriticalHitEvent e) {
        float vanillaMultiplier = e.getDamageModifier() / e.getOldDamageModifier();
        float critMultiplier = (float) e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_MULTIPLIER.get());
        if (e.isVanillaCritical()) {
            e.setDamageModifier(vanillaMultiplier * critMultiplier);
            return;
        }

        double critChance = e.getEntity().getAttributeValue(ManasCoreAttributes.CRIT_CHANCE.get()) / 100; // convert to %
        CriticalChanceEvent event = new CriticalChanceEvent(e.getEntity(), e.getTarget(), vanillaMultiplier * critMultiplier, critChance);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        RandomSource random = e.getEntity().getRandom();
        if (random.nextFloat() > event.getCritChance()) return; // Exit if this hit isn't a crit
        e.setDamageModifier(event.getDamageModifier());
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
            ArrowCriticalChanceEvent event = new ArrowCriticalChanceEvent(owner, arrow, critChance);

            if (MinecraftForge.EVENT_BUS.post(event)) return;
            arrow.setCritArrow(owner.getRandom().nextDouble() <= event.getCritChance());
        }
        arrow.getPersistentData().putBoolean("manascore.crit.calc.done", true);
    }

    @SubscribeEvent
    public static void modifyMiningSpeed(final PlayerEvent.BreakSpeed e) {
        AttributeInstance instance = e.getEntity().getAttribute(ManasCoreAttributes.MINING_SPEED_MULTIPLIER.get());
        if (instance == null) return;
        e.setNewSpeed((float) (e.getOriginalSpeed() * instance.getValue()));
    }
}
