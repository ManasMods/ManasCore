/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.network;

import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Client-side receiver for {@link PlayPlayerAnimationPayload}. Kept separate so the payload record
 * stays loader- and side-agnostic while {@code net.minecraft.client} references live here.
 */
public class AnimationClientHandler {

    static void handle(PlayPlayerAnimationPayload packet) {
        if (Minecraft.getInstance().level == null) return;
        Entity entity = getEntityFromId(packet.entityId());
        if (!(entity instanceof Player player)) return;

        PlayerAnimationAPI.PlayerAnimationState state = PlayerAnimationAPI.state(player);
        if (packet.animation().isEmpty()) {
            state.reset = true;
            state.firstPerson = false;
            state.currentAnimation = "";
            state.nextAnimation = "";
            state.hasProgress = false;
        } else {
            if (!PlayerAnimationAPI.canPlay(packet.animation(), state.currentAnimation)) return;
            state.currentAnimation = packet.animation();
            state.nextAnimation = packet.nextAnimation();
            state.override = packet.override();
            state.firstPerson = packet.firstPerson();
        }
    }

    private static Entity getEntityFromId(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == id) return mc.player;
        Level level = mc.level;
        if (level == null) return null;
        return level.getEntity(id);
    }
}
