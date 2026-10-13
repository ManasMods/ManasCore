/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.network;

import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * This is the client side receiver for {@link PlayPlayerAnimationPayload}.
 * It is kept separate so the payload record stays loader and side agnostic while the {@code net.minecraft.client} references live here.
 */
public class AnimationClientHandler {

    /**
     * Used to apply a received animation to the {@link LivingEntity} with the entity id of the packet.
     * Does nothing if there is no such entity, or if {@link PlayerAnimationAPI#canPlay} blocks the request.
     */
    static void handle(PlayPlayerAnimationPayload packet) {
        if (Minecraft.getInstance().level == null) return;
        Entity entity = getEntityFromId(packet.entityId());
        if (!(entity instanceof LivingEntity living)) return;

        PlayerAnimationAPI.PlayerAnimationState state = PlayerAnimationAPI.state(living);
        if (packet.animation().isEmpty()) {
            // Reset animation
            state.reset = true;
            state.firstPerson = false;
            state.currentAnimation = "";
            state.nextAnimation = "";
            state.hasProgress = false;
        } else {
            // Start animation
            if (!PlayerAnimationAPI.canPlay(packet.animation(), state.currentAnimation)) return;
            state.currentAnimation = packet.animation();
            state.nextAnimation = packet.nextAnimation();
            state.override = packet.override();
            state.firstPerson = packet.firstPerson();
        }
    }

    /**
     * Used to get the {@link Entity} of the given id from the client level. Returns null if there is none.
     */
    private static Entity getEntityFromId(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == id) return mc.player;
        Level level = mc.level;
        if (level == null) return null;
        return level.getEntity(id);
    }
}
