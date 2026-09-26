/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import net.minecraft.world.entity.Entity;

public interface AnimationEvents {
    Event<TriggerAnimationEvent> TRIGGER_ANIMATION_EVENT_EVENT = EventFactory.createEventResult();

    @FunctionalInterface
    interface TriggerAnimationEvent {
        EventResult trigger(Entity entity, Changeable<String> animation, Changeable<String> nextAnimation, Changeable<Boolean> override, Changeable<Boolean> firstPerson);
    }
}
