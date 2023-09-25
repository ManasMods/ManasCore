package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.TickingSkill;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillDamageEvent;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.UUID;

@EventBusSubscriber(modid = ManasCore.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class ServerEventListenerHandler {

    @SubscribeEvent
    public static void onEntityHurt(final LivingHurtEvent e) {
        if (e.getEntity().getLevel().isClientSide()) return;

        MinecraftForge.EVENT_BUS.post(new SkillDamageEvent.PreCalculation(e));
        MinecraftForge.EVENT_BUS.post(new SkillDamageEvent.Calculation(e));
        MinecraftForge.EVENT_BUS.post(new SkillDamageEvent.PostCalculation(e));

        SkillAPI.getSkillsFrom(e.getEntity()).syncChanges();
    }

    @SubscribeEvent
    public static void onPreBarrierDamage(final SkillDamageEvent.PreCalculation e) {
        if (e.getSource().getEntity() instanceof LivingEntity living) {
            SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
            for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
                if (!skillInstance.canInteractSkill(living)) continue;
                skillInstance.onDamageEntity(living, e.getEvent());
            }
        }
    }

    @SubscribeEvent
    public static void onPostBarrierDamage(final SkillDamageEvent.PostCalculation e) {
        if (e.getSource().getEntity() instanceof LivingEntity living) {
            SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
            for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
                if (!skillInstance.canInteractSkill(living)) continue;
                skillInstance.onTouchEntity(living, e.getEvent());
            }
        }
    }

    @SubscribeEvent
    public static void onBeingTargeted(final LivingChangeTargetEvent e) {
        LivingEntity target = e.getNewTarget();
        if (target == null) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(target);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(target)) continue;
            skillInstance.onBeingTargeted(target, e);
        }
        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onBeingDamaged(final LivingAttackEvent e) {
        final LivingEntity living = e.getEntity();
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(living)) continue;
            skillInstance.onBeingDamaged(e);
        }
        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onTakingReducedDamage(final LivingDamageEvent e) { //after applied everything, including armor, effects, etc.
        final LivingEntity living = e.getEntity();
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(living)) continue;
            skillInstance.onTakenDamage(e);
        }
        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onProjectileHit(final ProjectileImpactEvent e) {
        if (!(e.getRayTraceResult() instanceof EntityHitResult result)) return;
        if (!(result.getEntity() instanceof LivingEntity living)) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(living)) continue;
            skillInstance.onProjectileHit(living, e);
        }
    }

    @SubscribeEvent
    static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock e) {
        final Player player = e.getEntity();
        // In this case, we only want this to happen on Server Side
        if (player.getLevel().isClientSide()) return;
        // Get all listening skills and invoke their event consumer
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(player);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(player)) continue;
            skillInstance.onRightClickBlock(player, e.getHitVec());
        }
        //Sync changed Skills
        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onRespawn(final PlayerEvent.PlayerRespawnEvent e) {
        final Player player = e.getEntity();
        if (player.getLevel().isClientSide()) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(player);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(player)) continue;
            skillInstance.onRespawn(e);
        }

        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onDeath(final LivingDeathEvent e) {
        final LivingEntity living = e.getEntity();
        if (living.getLevel().isClientSide()) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (!skillInstance.canInteractSkill(living)) continue;
            skillInstance.onDeath(e);
        }
    }

    @SubscribeEvent
    public static void onLogOut(final PlayerEvent.PlayerLoggedOutEvent e) {
        Player player = e.getEntity();
        Multimap<UUID, TickingSkill> multimap = TickEventListenerHandler.tickingSkills;
        if (multimap.containsKey(player.getUUID())) multimap.removeAll(player.getUUID());
    }
}