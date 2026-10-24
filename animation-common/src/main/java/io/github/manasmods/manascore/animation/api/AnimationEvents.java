/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import net.minecraft.world.entity.Entity;

/**
 * This holds the events of the animation module.
 */
public interface AnimationEvents {
    /**
     * Fired on the server before an animation is sent to the tracking clients.
     * Listeners can change the values through the {@link Changeable} parameters.
     *
     * @see PlayerAnimationHelper#play(Entity, String, String, boolean, boolean)
     */
    Event<TriggerAnimationEvent> TRIGGER_ANIMATION_EVENT_EVENT = EventFactory.createEventResult();

    @FunctionalInterface
    interface TriggerAnimationEvent {
        /**
         * Called when an animation is about to be played on an {@link Entity}.
         *
         * @param entity        Affected {@link Entity} playing the animation.
         * @param animation     Animation key ({@code <modid>:<name>}). An empty string resets the active animation.
         * @param nextAnimation Animation key that plays automatically once {@code animation} finishes. Empty for none.
         * @param override      true restarts the animation from the beginning.
         * @param firstPerson   true shows the animation from the first person view of the owner.
         * @return false will stop the animation from being sent.
         */
        EventResult trigger(Entity entity, Changeable<String> animation, Changeable<String> nextAnimation, Changeable<Boolean> override, Changeable<Boolean> firstPerson);
    }
}
