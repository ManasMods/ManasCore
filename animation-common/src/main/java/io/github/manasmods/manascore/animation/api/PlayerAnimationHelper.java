/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.animation.network.PlayPlayerAnimationPayload;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.network.api.util.PlayerLookup;
import net.minecraft.world.entity.Entity;

/**
 * This is the server side entry point for playing Bedrock player animations on tracking clients.
 * It is safe to reference on a dedicated server, unlike {@link PlayerAnimationAPI} which touches client classes.
 */
public final class PlayerAnimationHelper {
    private PlayerAnimationHelper() {}

    /**
     * Used to play the given animation on the {@link Entity} for all tracking players and the entity itself.
     * Does nothing when called on the client.
     * <p>
     * Listeners of {@link AnimationEvents#TRIGGER_ANIMATION_EVENT_EVENT} can change the values or stop the animation.
     *
     * @param entity        Affected {@link Entity} playing the animation.
     * @param animation     Animation key ({@code <modid>:<name>}). An empty string resets the active animation.
     * @param nextAnimation Animation key that plays automatically once {@code animation} finishes. Empty for none.
     * @param override      true restarts the animation from the beginning.
     * @param firstPerson   true shows the animation from the first person view of the owner.
     */
    public static void play(Entity entity, String animation, String nextAnimation, boolean override, boolean firstPerson) {
        if (entity.level().isClientSide()) return;
        Changeable<String> animationChangeable = Changeable.of(animation);
        Changeable<String> nextAnimationChangeable = Changeable.of(nextAnimation);
        Changeable<Boolean> overrideChangeable = Changeable.of(override);
        Changeable<Boolean> firstPersonChangeable = Changeable.of(firstPerson);

        if (AnimationEvents.TRIGGER_ANIMATION_EVENT_EVENT.invoker().trigger(entity, animationChangeable, nextAnimationChangeable, overrideChangeable, firstPersonChangeable).isFalse()) return;
        PlayPlayerAnimationPayload payload = new PlayPlayerAnimationPayload(entity.getId(), animationChangeable.get(), nextAnimationChangeable.get(), overrideChangeable.get(), firstPersonChangeable.get());
        NetworkManager.sendToPlayers(PlayerLookup.trackingAndSelf(entity), payload);
    }

    /**
     * Used to play the given animation without a follow up animation.
     *
     * @see #play(Entity, String, String, boolean, boolean)
     */
    public static void play(Entity entity, String animation, boolean override, boolean firstPerson) {
        play(entity, animation, "", override, firstPerson);
    }

    /**
     * Used to play the given animation without a follow up animation, restarted and shown in first person.
     *
     * @see #play(Entity, String, String, boolean, boolean)
     */
    public static void play(Entity entity, String animation) {
        play(entity, animation, "", true, true);
    }

    /**
     * Used to play {@code animation} once and play {@code nextAnimation} automatically when it finishes.
     * Use a looping {@code nextAnimation} for charge style holds, an intro followed by a loop until {@link #stop}.
     */
    public static void playThenLoop(Entity entity, String animation, String nextAnimation) {
        play(entity, animation, nextAnimation, true, true);
    }

    /**
     * Used to reset any active animation on the {@link Entity}.
     */
    public static void stop(Entity entity) {
        play(entity, "", "", true, true);
    }
}
