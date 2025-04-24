/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl;

import io.github.manasmods.manascore.race.api.ManasRace;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.Races;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * This is the Registry Object for Ticking Races when a {@link ManasRace} is held down.
 */
public class TickingRace {
    private int duration = 0;
    public TickingRace() {
    }

    public boolean tick(Races storage, LivingEntity entity) {
        if (!entity.isAlive()) return false;
        Optional<ManasRaceInstance> optional = storage.getRace();
        if (optional.isEmpty()) return false;

        ManasRaceInstance instance = optional.get();
        if (reachedMaxDuration(instance, entity)) return false;

        if (!instance.canActivateAbility(entity)) return false;
        return instance.onHeldAbility(entity, this.duration++);
    }

    public boolean reachedMaxDuration(ManasRaceInstance instance, LivingEntity entity) {
        int maxDuration = instance.getMaxHeldTime(entity);
        if (maxDuration == -1) return false;
        return duration >= maxDuration;
    }
}
