/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network;

import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillActivationPacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillReleasePacket;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class InternalSkillPacketActions {
    private InternalSkillPacketActions() {
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillActivationEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillActivationPacket(int keyNumber) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        List<ResourceLocation> packetSkills = new ArrayList<>();

        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
            if (SkillEvents.ACTIVATE_SKILL.invoker().activateSkill(skillInstance, player, keyNumber).isFalse()) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        NetworkManager.sendToServer(new RequestSkillActivationPacket(keyNumber, packetSkills));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillReleaseEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillReleasePacket(int keyNumber, int heldTicks) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        List<ResourceLocation> packetSkills = new ArrayList<>();

        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
            if (SkillEvents.RELEASE_SKILL.invoker().releaseSkill(skillInstance, player, keyNumber, heldTicks).isFalse()) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        NetworkManager.sendToServer(new RequestSkillReleasePacket(heldTicks, keyNumber, packetSkills));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillEvents.SkillToggleEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillTogglePacket() {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        List<ResourceLocation> packetSkills = new ArrayList<>();

        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
            if (!skillInstance.canBeToggled(player)) continue;
            if (SkillEvents.TOGGLE_SKILL.invoker().toggleSkill(skillInstance, player).isFalse()) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        NetworkManager.sendToServer(new RequestSkillTogglePacket(packetSkills));
    }
}
