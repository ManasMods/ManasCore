/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;

public interface RaceEvents {
    Event<SetRaceEvent> SET_RACE = EventFactory.createEventResult();
    Event<RaceTickEvent> SKILL_PRE_TICK = EventFactory.createEventResult();
    Event<RacePostTickEvent> SKILL_POST_TICK = EventFactory.createLoop();
    Event<RaceAbilityActivationEvent> ACTIVATE_ABILITY = EventFactory.createEventResult();

    @FunctionalInterface
    interface SetRaceEvent {
        EventResult set(ManasRaceInstance instance, LivingEntity owner, ManasRaceInstance newInstance, boolean evolution);
    }

    @FunctionalInterface
    interface RaceTickEvent {
        EventResult tick(ManasRaceInstance instance, LivingEntity owner);
    }

    @FunctionalInterface
    interface RacePostTickEvent {
        void tick(ManasRaceInstance instance, LivingEntity owner);
    }

    @FunctionalInterface
    interface RaceAbilityActivationEvent {
        EventResult activateAbility(ManasRaceInstance instance, LivingEntity owner);
    }
}
