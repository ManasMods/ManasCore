package com.github.manasmods.manascore.skill;

import com.github.manasmods.manascore.api.skill.ManasSkill;
import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.api.skill.SkillEvents.SkillActivationEvent;
import com.github.manasmods.manascore.api.skill.SkillEvents.SkillReleaseEvent;
import com.github.manasmods.manascore.api.skill.SkillEvents.SkillToggleEvent;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.network.toserver.RequestSkillActivationPacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillReleasePacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class InternalSkillPacketActions {
    private InternalSkillPacketActions() {
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillActivationEvent} then send packet for them.
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
        NetworkManager.CHANNEL.sendToServer(new RequestSkillActivationPacket(packetSkills, keyNumber));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillReleaseEvent} then send packet for them.
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
        NetworkManager.CHANNEL.sendToServer(new RequestSkillReleasePacket(packetSkills, keyNumber, heldTicks));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillToggleEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillTogglePacket() {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        List<ResourceLocation> packetSkills = new ArrayList<>();

        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
            if (SkillEvents.TOGGLE_SKILL.invoker().toggleSkill(skillInstance, player).isFalse()) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        NetworkManager.CHANNEL.sendToServer(new RequestSkillTogglePacket(packetSkills));
    }
}
