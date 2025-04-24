/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.race.impl.RaceRegistry;
import io.github.manasmods.manascore.race.impl.RaceStorage;
import io.github.manasmods.manascore.race.impl.network.InternalRacePacketActions;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RaceAPI {
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
        return entity.manasCore$getStorage(RaceStorage.getKey());
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
    public static void raceAbilityReleasePacket(int heldTick) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            InternalRacePacketActions.sendRaceAbilityReleasePacket(heldTick);
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
