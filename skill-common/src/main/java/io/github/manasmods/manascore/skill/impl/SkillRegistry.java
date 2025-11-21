/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.api.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public class SkillRegistry {
    private static final ResourceLocation registryId = ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "skills");
    public static final Registrar<ManasSkill> SKILLS = RegistrarManager.get(ModuleConstants.MOD_ID).<ManasSkill>builder(registryId).syncToClients().build();
    public static final ResourceKey<Registry<ManasSkill>> KEY = ResourceKey.createRegistryKey(SKILLS.key().location());

    public static void init() {
        EntityEvents.LIVING_EFFECT_ADDED.register((entity, source, changeableTarget) -> {
            Skills storage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onEffectAdded(entity, source, changeableTarget)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET.register((entity, changeableTarget) -> {
            if (!changeableTarget.isPresent()) return EventResult.pass();
            LivingEntity owner = changeableTarget.get();
            if (owner == null) return EventResult.pass();

            Skills storage = SkillAPI.getSkillsFrom(owner);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onBeingTargeted(changeableTarget, entity)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        EntityEvents.LIVING_ON_BEING_DAMAGED.register((entity, source, amount) -> {
            Skills storage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onBeingDamaged(entity, source, amount)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_PRE_CALCULATION.register((storage, target, source, amount) -> {
            if (!(source.getEntity() instanceof LivingEntity owner)) return EventResult.pass();

            Skills ownerStorage = SkillAPI.getSkillsFrom(owner);
            for (ManasSkillInstance instance : ownerStorage.getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onDamageEntity(owner, target, source, amount)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_POST_CALCULATION.register((storage, target, source, amount) -> {
            if (!(source.getEntity() instanceof LivingEntity owner)) return EventResult.pass();

            Skills ownerStorage = SkillAPI.getSkillsFrom(owner);
            for (ManasSkillInstance instance : ownerStorage.getLearnedSkills()) {
                if (!instance.canInteractSkill(owner)) continue;
                if (!instance.onTouchEntity(owner, target, source, amount)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        EntityEvents.LIVING_DAMAGE.register((entity, source, amount) -> {
            Skills storage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onTakenDamage(entity, source, amount)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        EntityEvents.DEATH_EVENT_HIGH.register((entity, source) -> {
            Skills storage = SkillAPI.getSkillsFrom(entity);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(entity)) continue;
                if (!instance.onDeath(entity, source)) {
                    storage.checkAndMarkDirty(instance);
                    return EventResult.interruptFalse();
                } else storage.checkAndMarkDirty(instance);
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd, removalReason) -> {
            Skills storage = SkillAPI.getSkillsFrom(newPlayer);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(newPlayer)) continue;
                instance.onRespawn(newPlayer, conqueredEnd);
                storage.checkAndMarkDirty(instance);
            }
        });

        EntityEvents.PROJECTILE_HIT.register((result, projectile, deflectionChangeable, hitResultChangeable) -> {
            if (!(result instanceof EntityHitResult hitResult)) return;
            if (!(hitResult.getEntity() instanceof LivingEntity hitEntity)) return;

            Skills storage = SkillAPI.getSkillsFrom(hitEntity);
            for (ManasSkillInstance instance : storage.getLearnedSkills()) {
                if (!instance.canInteractSkill(hitEntity)) continue;
                instance.onProjectileHit(hitEntity, hitResult, projectile, deflectionChangeable, hitResultChangeable);
                storage.checkAndMarkDirty(instance);
            }
        });
    }

    private SkillRegistry() {
    }
}
