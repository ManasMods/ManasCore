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

    default boolean setRace(@NotNull ResourceLocation raceId) {
        return setRace(RaceAPI.getRaceRegistry().get(raceId).createDefaultInstance(), false);
    }

    default boolean setRace(@NonNull ManasRace skill) {
        return setRace(skill.createDefaultInstance(), false);
    }

    boolean setRace(ManasRaceInstance instance, boolean evolution);

    default boolean evolveRace(@NotNull ResourceLocation raceId) {
        return setRace(RaceAPI.getRaceRegistry().get(raceId).createDefaultInstance(), true);
    }

    default boolean evolveRace(@NonNull ManasRace skill) {
        return setRace(skill.createDefaultInstance(), true);
    }

    default boolean evolveRace(ManasRaceInstance evolution) {
        return setRace(evolution, true);
    }

    void markDirty();
}
