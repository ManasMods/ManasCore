/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.animation.network.PlayPlayerAnimationPayload;
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
    public static void play(Entity entity, String animation, boolean override, boolean firstPerson) {
        if (entity.level().isClientSide()) return;
        PlayPlayerAnimationPayload payload = new PlayPlayerAnimationPayload(entity.getId(), animation, override, firstPerson);
        NetworkManager.sendToPlayers(PlayerLookup.trackingAndSelf(entity), payload);
    }

    public static void play(Entity entity, String animation) {
        play(entity, animation, true, true);
    }

    /**
     * Resets any active animation on the entity.
     */
    public static void stop(Entity entity) {
        play(entity, "", true, true);
    }
}
