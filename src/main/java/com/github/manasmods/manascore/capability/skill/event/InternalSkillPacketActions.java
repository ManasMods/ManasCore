package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.api.skills.ManasSkill;
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
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.AvailableSince("2.0.18.0")
public final class InternalSkillPacketActions {
    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillActivationEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillActivationPacket(ResourceLocation skill, int keyNumber) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillActivationPacket(skill, keyNumber));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillReleaseEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillReleasePacket(ResourceLocation skill, int keyNumber, int heldTicks) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillReleasePacket(skill, keyNumber, heldTicks));
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillToggleEvent} then send packet for them.
     * Only executes on client using the dist executor.
     */
    public static void sendSkillTogglePacket(ResourceLocation skill) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillTogglePacket(skill));
    }
}
