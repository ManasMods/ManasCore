package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.event.SkillActivationEvent;
import com.github.manasmods.manascore.api.skills.event.SkillReleaseEvent;
import com.github.manasmods.manascore.api.skills.event.SkillToggleEvent;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.network.toserver.RequestSkillActivationPacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillReleasePacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
@ApiStatus.AvailableSince("2.0.18.0")
public final class InternalSkillPacketActions {
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
            SkillActivationEvent event = new SkillActivationEvent(skillInstance, player, keyNumber);
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillActivationPacket(packetSkills, keyNumber));
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
            SkillReleaseEvent event = new SkillReleaseEvent(skillInstance, player, keyNumber, heldTicks);
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillReleasePacket(packetSkills, keyNumber, heldTicks));
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
            SkillToggleEvent event = new SkillToggleEvent(skillInstance, player, !skillInstance.isToggled());
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillTogglePacket(packetSkills));
    }
}
