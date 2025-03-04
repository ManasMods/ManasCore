/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public class SkillRegistry {
    private static final ResourceLocation registryId = ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "skills");
    public static final Registrar<ManasSkill> SKILLS = RegistrarManager.get(ModuleConstants.MOD_ID).<ManasSkill>builder(registryId)
            .syncToClients()
            .build();
    public static final ResourceKey<Registry<ManasSkill>> KEY = (ResourceKey<Registry<ManasSkill>>) SKILLS.key();

    public static void init() {
        EntityEvents.LIVING_EFFECT_ADDED.register((entity, source, changeableTarget) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onEffectAdded(entity, source, changeableTarget)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET.register((entity, changeableTarget) -> {
            if (!changeableTarget.isPresent()) return EventResult.pass();
            LivingEntity owner = changeableTarget.get();
            if (owner == null) return EventResult.pass();

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(owner).getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onBeingTargeted(changeableTarget, entity)) return EventResult.interruptFalse();
            }

            return EventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
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

        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd, removalReason) -> {
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(newPlayer).getLearnedSkills()) {
                if (!instance.canInteractSkill(newPlayer)) continue;
                instance.onRespawn(newPlayer, conqueredEnd);
            }
        });

        EntityEvents.PROJECTILE_HIT.register((result, projectile, deflectionChangeable, hitResultChangeable) -> {
            if (!(result instanceof EntityHitResult hitResult)) return;
            if (!(hitResult.getEntity() instanceof LivingEntity hitEntity)) return;

            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(hitEntity).getLearnedSkills()) {
                if (!instance.canInteractSkill(hitEntity)) continue;
                instance.onProjectileHit(hitEntity, hitResult, projectile, deflectionChangeable, hitResultChangeable);
            }
        });
    }

    private SkillRegistry() {
    }
}
