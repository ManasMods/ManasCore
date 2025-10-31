/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.*;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class RaceRegistry {
    private static final ResourceLocation registryId = ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "races");
    public static final Registrar<ManasRace> RACES = RegistrarManager.get(ModuleConstants.MOD_ID).<ManasRace>builder(registryId).syncToClients().build();
    public static final ResourceKey<Registry<ManasRace>> KEY = (ResourceKey<Registry<ManasRace>>) RACES.key();

    public static void init() {
        EntityEvents.LIVING_EFFECT_ADDED.register((entity, source, changeableTarget) -> {
            Races storage = RaceAPI.getRaceFrom(entity);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return EventResult.pass();

            ManasRaceInstance instance = optional.get();
            if (!instance.canActivateAbility(entity)) return EventResult.pass();
            if (!instance.onEffectAdded(entity, source, changeableTarget)) {
                storage.checkAndMarkDirty(instance);
                return EventResult.interruptFalse();
            } else storage.checkAndMarkDirty(instance);
            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET.register((entity, changeableTarget) -> {
            if (!changeableTarget.isPresent()) return EventResult.pass();
            LivingEntity owner = changeableTarget.get();
            if (owner == null) return EventResult.pass();

            Races storage = RaceAPI.getRaceFrom(owner);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return EventResult.pass();

            ManasRaceInstance instance = optional.get();
            if (!instance.canActivateAbility(owner)) return EventResult.pass();
            if (!instance.onBeingTargeted(changeableTarget, entity)) {
                storage.checkAndMarkDirty(instance);
                return EventResult.interruptFalse();
            } else storage.checkAndMarkDirty(instance);
            return EventResult.pass();
        });

        SkillEvents.SKILL_DAMAGE_POST_CALCULATION.register((storage, target, source, amount) -> {
            if (!(source.getEntity() instanceof LivingEntity owner)) return EventResult.pass();
            Races ownerStorage = RaceAPI.getRaceFrom(owner);
            Optional<ManasRaceInstance> optional = ownerStorage.getRace();
            if (optional.isEmpty()) return EventResult.pass();

            ManasRaceInstance instance = optional.get();
            if (!instance.canActivateAbility(owner)) return EventResult.pass();
            if (!instance.onAttackEntity(owner, target, source, amount)) {
                ownerStorage.checkAndMarkDirty(instance);
                return EventResult.interruptFalse();
            } else ownerStorage.checkAndMarkDirty(instance);
            return EventResult.pass();
        });

        EntityEvents.LIVING_DAMAGE.register((entity, source, amount) -> {
            Races storage = RaceAPI.getRaceFrom(entity);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return EventResult.pass();

            ManasRaceInstance instance = optional.get();
            if (!instance.canActivateAbility(entity)) return EventResult.pass();
            if (!instance.onHurt(entity, source, amount)) {
                storage.checkAndMarkDirty(instance);
                return EventResult.interruptFalse();
            } else storage.checkAndMarkDirty(instance);
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            Races storage = RaceAPI.getRaceFrom(entity);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return EventResult.pass();

            ManasRaceInstance instance = optional.get();
            if (!instance.canActivateAbility(entity)) return EventResult.pass();
            if (!instance.onDeath(entity, source)) {
                storage.checkAndMarkDirty(instance);
                return EventResult.interruptFalse();
            } else storage.checkAndMarkDirty(instance);
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd, removalReason) -> {
            Races storage = RaceAPI.getRaceFrom(newPlayer);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return;

            ManasRaceInstance instance = optional.get();
            if (!conqueredEnd) {
                instance.addAttributeModifiers(newPlayer);
                newPlayer.setHealth(newPlayer.getMaxHealth());
                SpawnPointHelper.teleportToNewSpawn(newPlayer);
            }

            if (!instance.canActivateAbility(newPlayer)) return;
            instance.onRespawn(newPlayer, conqueredEnd);
            storage.checkAndMarkDirty(instance);
        });
    }

    private RaceRegistry() {
    }
}
