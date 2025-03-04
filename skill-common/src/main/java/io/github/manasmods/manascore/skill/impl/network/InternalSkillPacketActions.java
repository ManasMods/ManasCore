/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillActivationPacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillReleasePacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class InternalSkillPacketActions {
    private InternalSkillPacketActions() {
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillActivationEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillActivationPacket(ResourceLocation skillId, int keyNumber, int mode) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestSkillActivationPacket(keyNumber, skillId, mode));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillReleaseEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillReleasePacket(ResourceLocation skillId, int keyNumber, int mode, int heldTicks) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestSkillReleasePacket(heldTicks, keyNumber, mode, skillId));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillToggleEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillTogglePacket(ResourceLocation skillId) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        NetworkManager.sendToServer(new RequestSkillTogglePacket(skillId));
    }
}
