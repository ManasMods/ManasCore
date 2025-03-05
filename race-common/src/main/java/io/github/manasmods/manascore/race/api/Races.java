/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Races {
    Optional<ManasRaceInstance> getRace();

    default boolean setRace(@NotNull ResourceLocation raceId, boolean teleportToSpawn) {
        ManasRace race = RaceAPI.getRaceRegistry().get(raceId);
        if (race == null) return false;
        return setRace(race.createDefaultInstance(), false, teleportToSpawn);
    }

    default boolean setRace(@NonNull ManasRace race, boolean teleportToSpawn) {
        return setRace(race.createDefaultInstance(), false, teleportToSpawn);
    }

    boolean setRace(ManasRaceInstance instance, boolean evolution, boolean teleportToSpawn);

    default boolean evolveRace(@NotNull ResourceLocation raceId) {
        ManasRace race = RaceAPI.getRaceRegistry().get(raceId);
        if (race == null) return false;
        return setRace(race.createDefaultInstance(), true, false);
    }

    default boolean evolveRace(@NonNull ManasRace race) {
        return setRace(race.createDefaultInstance(), true, false);
    }

    default boolean evolveRace(ManasRaceInstance evolution) {
        return setRace(evolution, true, false);
    }

    void markDirty();
}
