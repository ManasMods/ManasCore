package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.api.world.entity.EntityEvents;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public class SkillRegistry {
    private static final ResourceLocation registryId = new ResourceLocation(ManasCore.MOD_ID, "skills");
    public static final Registrar<ManasSkill> SKILLS = RegistrarManager.get(ManasCore.MOD_ID).<ManasSkill>builder(registryId)
            .syncToClients()
            .build();
    public static final ResourceKey<Registry<ManasSkill>> KEY = (ResourceKey<Registry<ManasSkill>>) SKILLS.key();


    public static void init() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            SkillAPI.getSkillsFrom(player).forEachSkill(((storage, skillInstance) -> {
                if (!skillInstance.canInteractSkill(player)) return;
                skillInstance.onRightClickBlock(player, hand, pos, face);
            }));

            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET.register((entity, changeableTarget) -> {
            if (!changeableTarget.isPresent()) return EventResult.pass();

            LivingEntity owner = changeableTarget.get();
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(owner).getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onBeingTargeted(changeableTarget, entity)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvents.LIVING_ATTACK.register((entity, source, amount) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onBeingDamaged(entity, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_PRE_CALCULATION.register((storage, target, source, amount) -> {
            if (!(source.getEntity() instanceof LivingEntity owner)) return EventResult.pass();

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(owner).getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onDamageEntity(owner, target, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_POST_CALCULATION.register((storage, target, source, amount) -> {
            if (!(source.getEntity() instanceof LivingEntity owner)) return EventResult.pass();

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(owner).getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onTouchEntity(owner, target, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvents.LIVING_DAMAGE.register((entity, source, amount) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onTakenDamage(entity, source, amount)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onDeath(entity, source)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(newPlayer).getLearnedSkills()) {
                if (!instance.canInteractSkill(newPlayer)) continue;
                instance.onRespawn(newPlayer, conqueredEnd);
            }
        });

        EntityEvents.PROJECTILE_HIT.register((result, projectile, hitResultChangeable) -> {
            if (!(result instanceof EntityHitResult hitResult)) return;
            if (!(hitResult.getEntity() instanceof LivingEntity hitEntity)) return;

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(hitEntity).getLearnedSkills()) {
                if (!instance.canInteractSkill(hitEntity)) continue;
                instance.onProjectileHit(hitEntity, hitResult, projectile, hitResultChangeable);
            }
        });
    }

    private SkillRegistry() {
    }
}
