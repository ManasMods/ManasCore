/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.race.ManasCoreRace;
import io.github.manasmods.manascore.race.impl.RaceRegistry;
import io.github.manasmods.manascore.race.impl.RaceStorage;
import io.github.manasmods.manascore.race.impl.network.InternalRacePacketActions;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RaceAPI {
    private static final Set<EntityType<?>> MISSING_STORAGE_WARNED = ConcurrentHashMap.newKeySet();

    private RaceAPI() {
    }

    /**
     * This Method returns the {@link ManasRace} Registry.
     * It can be used to load {@link ManasRace}s from the Registry.
     */
    public static Registrar<ManasRace> getRaceRegistry() {
        return RaceRegistry.RACES;
    }

    /**
     * This Method returns the Registry Key of the {@link RaceRegistry}.
     * It can be used to create {@link dev.architectury.registry.registries.DeferredRegister} instances
     */
    public static ResourceKey<Registry<ManasRace>> getRaceRegistryKey() {
        return RaceRegistry.KEY;
    }

    /**
     * Can be used to load the {@link RaceStorage} from an {@link LivingEntity}.
     */
    public static Races getRaceFrom(@NonNull LivingEntity entity) {
        Races storage = entity.manasCore$getStorage(RaceStorage.getKey());
        if (storage != null) return storage;
        if (MISSING_STORAGE_WARNED.add(entity.getType())) {
            ManasCoreRace.LOG.warn("Race storage is missing on entity type {} - falling back to a no-op storage. This usually means the storage failed to attach or sync.", EntityType.getKey(entity.getType()));
        }
        return Races.EMPTY;
    }

    /**
     * Send {@link InternalRacePacketActions#sendRaceAbilityActivationPacket} with a DistExecutor on client side.
     * Used when player activates the Race Ability.
     *
     * @see InternalRacePacketActions#sendRaceAbilityActivationPacket
     */
    public static void raceAbilityActivationPacket() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalRacePacketActions.sendRaceAbilityActivationPacket();
        }
    }

    /**
     * Send {@link InternalRacePacketActions#sendRaceAbilityReleasePacket} with a DistExecutor on client side.
     * Used when player releases the Race Ability.
     *
     * @see InternalRacePacketActions#sendRaceAbilityReleasePacket
     */
    public static void raceAbilityReleasePacket() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalRacePacketActions.sendRaceAbilityReleasePacket();
        }
    }

    /**
     * Send {@link InternalRacePacketActions#sendRaceEvolutionPacket} with a DistExecutor on client side.
     * Used when player evolves into a race.
     *
     * @see InternalRacePacketActions#sendRaceEvolutionPacket
     */
    public static void raceEvolutionPacket(ResourceLocation location) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalRacePacketActions.sendRaceEvolutionPacket(location);
        }
    }
}
