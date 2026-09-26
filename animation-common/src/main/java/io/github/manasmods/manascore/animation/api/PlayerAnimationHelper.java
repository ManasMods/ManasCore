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
 * Server-side entry point for playing Bedrock player animations on tracking clients.
 * Safe to reference on a dedicated server (unlike {@link PlayerAnimationAPI}, which touches client classes).
 */
public final class PlayerAnimationHelper {
    private PlayerAnimationHelper() {}

    /**
     * Plays the given animation ({@code <modid>:<name>}) on the entity for all tracking players and the entity itself.
     * No-op when called on the client.
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

    public static void play(Entity entity, String animation, boolean override, boolean firstPerson) {
        play(entity, animation, "", override, firstPerson);
    }

    public static void play(Entity entity, String animation) {
        play(entity, animation, "", true, true);
    }

    /**
     * Plays {@code animation} once, then automatically plays {@code nextAnimation} when it finishes.
     * Use a looping {@code nextAnimation} for charge-style holds (intro → loop until {@link #stop}).
     */
    public static void playThenLoop(Entity entity, String animation, String nextAnimation) {
        play(entity, animation, nextAnimation, true, true);
    }

    /**
     * Resets any active animation on the entity.
     */
    public static void stop(Entity entity) {
        play(entity, "", "", true, true);
    }
}
