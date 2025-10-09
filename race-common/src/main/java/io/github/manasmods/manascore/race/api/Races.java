/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import lombok.NonNull;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface Races {
    Optional<ManasRaceInstance> getRace();

    default boolean setRace(@NotNull ResourceLocation raceId, boolean teleportToSpawn) {
        return setRace(raceId, teleportToSpawn, null);
    }

    default boolean setRace(@NotNull ResourceLocation raceId, boolean teleportToSpawn, @Nullable MutableComponent component) {
        ManasRace race = RaceAPI.getRaceRegistry().get(raceId);
        if (race == null) return false;
        return setRace(race.createDefaultInstance(), false, teleportToSpawn, component);
    }

    default boolean setRace(@NonNull ManasRace race, boolean teleportToSpawn) {
        return setRace(race, teleportToSpawn, null);
    }

    default boolean setRace(@NonNull ManasRace race, boolean teleportToSpawn, @Nullable MutableComponent component) {
        return setRace(race.createDefaultInstance(), false, teleportToSpawn, component);
    }

    default boolean setRace(ManasRaceInstance instance, boolean evolution, boolean teleportToSpawn) {
        return setRace(instance, evolution, teleportToSpawn, null);
    }

    boolean setRace(ManasRaceInstance instance, boolean evolution, boolean teleportToSpawn, @Nullable MutableComponent component);

    default boolean evolveRace(@NotNull ResourceLocation raceId) {
        return evolveRace(raceId, null);
    }

    default boolean evolveRace(@NotNull ResourceLocation raceId, @Nullable MutableComponent component) {
        ManasRace race = RaceAPI.getRaceRegistry().get(raceId);
        if (race == null) return false;
        return setRace(race.createDefaultInstance(), true, false);
    }

    default boolean evolveRace(@NonNull ManasRace race) {
        return evolveRace(race, null);
    }

    default boolean evolveRace(@NonNull ManasRace race, @Nullable MutableComponent component) {
        return setRace(race.createDefaultInstance(), true, false, component);
    }

    default boolean evolveRace(ManasRaceInstance evolution) {
        return evolveRace(evolution, null);
    }

    default boolean evolveRace(ManasRaceInstance evolution, @Nullable MutableComponent component) {
        return setRace(evolution, true, false, component);
    }

    void markDirty();

    default void checkAndMarkDirty(ManasRaceInstance instance) {
        if (instance.isDirty()) this.markDirty();
    }
}
