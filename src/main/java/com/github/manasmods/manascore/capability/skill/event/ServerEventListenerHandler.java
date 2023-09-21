package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.BarrierNegateDamageEvent;
import com.github.manasmods.manascore.api.skills.event.DamagePostBarrierEvent;
import com.github.manasmods.manascore.api.skills.event.DamagePreBarrierEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ManasCore.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class ServerEventListenerHandler {

    @SubscribeEvent
    public static void onEntityHurt(final LivingHurtEvent e) {
        DamagePreBarrierEvent preBarrierEvent = new DamagePreBarrierEvent(e);
        if (MinecraftForge.EVENT_BUS.post(preBarrierEvent)) {
            e.setCanceled(true);
        } else {
            e.setAmount(preBarrierEvent.getAmount());
        }

        BarrierNegateDamageEvent barrierEvent = new BarrierNegateDamageEvent(e);
        if (!MinecraftForge.EVENT_BUS.post(barrierEvent)) e.setAmount(barrierEvent.getAmount());

        DamagePostBarrierEvent postBarrierEvent = new DamagePostBarrierEvent(e);
        if (MinecraftForge.EVENT_BUS.post(postBarrierEvent)) {
            e.setCanceled(true);
        } else {
            e.setAmount(postBarrierEvent.getAmount());
        }
    }

    @SubscribeEvent
    public static void onPreBarrierDamage(final DamagePreBarrierEvent e) {
        final LivingEntity entity = e.getEntity();
        if (e.getSource().getEntity() instanceof LivingEntity living) {
            SkillStorage skillStorage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
                if (skillInstance.canInteractSkill(living)) continue;
                e.setAmount(skillInstance.onDamageEntity(living, entity, e.getEvent()));
            }
            skillStorage.syncChanges();
        }
    }

    @SubscribeEvent
    public static void onPostBarrierDamage(final DamagePostBarrierEvent e) {
        final LivingEntity entity = e.getEntity();
        if (e.getSource().getEntity() instanceof LivingEntity living) {
            SkillStorage skillStorage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
                if (skillInstance.canInteractSkill(living)) continue;
                e.setAmount(skillInstance.onTouchEntity(living, entity, e.getEvent()));
            }
            skillStorage.syncChanges();
        }
    }

    @SubscribeEvent
    public static void onTakingReducedDamage(final LivingDamageEvent e) { //after applied everything, including armor, effects, etc.
        final LivingEntity living = e.getEntity();
        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (skillInstance.canInteractSkill(living)) continue;
            e.setAmount(skillInstance.onTakenDamage(living, e));
        }
        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onProjectileHit(final ProjectileImpactEvent e) {
        if (!(e.getRayTraceResult() instanceof EntityHitResult result)) return;
        if (!(result.getEntity() instanceof LivingEntity living)) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (skillInstance.canInteractSkill(living)) continue;

            if (skillInstance.onProjectileHit(living, e)) {
                skillStorage.syncChanges();
                e.setCanceled(true);
            }
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
            if (skillInstance.canInteractSkill(player)) continue;
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
            if (skillInstance.canInteractSkill(player)) continue;
            skillInstance.onRespawn(skillInstance, e);
        }

        skillStorage.syncChanges();
    }

    @SubscribeEvent
    public static void onDeath(final LivingDeathEvent e) {
        final LivingEntity living = e.getEntity();
        if (living.getLevel().isClientSide()) return;

        SkillStorage skillStorage = SkillAPI.getSkillsFrom(living);
        for (ManasSkillInstance skillInstance : skillStorage.getLearnedSkills()) {
            if (skillInstance.canInteractSkill(living)) continue;

            if (skillInstance.onDeath(living, e)) {
                skillStorage.syncChanges();
                e.setCanceled(true);
            }
        }
    }
}