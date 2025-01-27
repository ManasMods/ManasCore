/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface AttributeEvents {
    Event<CriticalAttackChanceEvent> CRITICAL_ATTACK_CHANCE_EVENT = EventFactory.createEventResult();

    @FunctionalInterface
    interface CriticalAttackChanceEvent {
        EventResult applyCrit(LivingEntity attacker, Entity target, float originalMultiplier, Changeable<Float> multiplier, Changeable<Double> chance);
    }
}
