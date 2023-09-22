package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillActivationEvent;
import com.github.manasmods.manascore.api.skills.event.SkillReleaseEvent;
import com.github.manasmods.manascore.api.skills.event.SkillToggleEvent;
import com.github.manasmods.manascore.capability.skill.EntitySkillCapability;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.network.toserver.RequestSkillActivationPacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillReleasePacket;
import com.github.manasmods.manascore.network.toserver.RequestSkillTogglePacket;
import com.github.manasmods.manascore.skill.SkillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.NonExtendable
@ApiStatus.AvailableSince("1.0.2.0")
public final class SkillAPI {
    /**
     * This Method returns the {@link ManasSkill} Registry.
     * It can be used to load {@link ManasSkill}s from the Registry.
     */
    @NotNull
    public static IForgeRegistry<ManasSkill> getSkillRegistry() {
        return SkillRegistry.REGISTRY.get();
    }

    /**
     * This Method returns the Registry Key of the {@link SkillRegistry}.
     * It can be used to create {@link DeferredRegister} instances
     */
    @NotNull
    public static ResourceLocation getSkillRegistryKey() {
        return SkillRegistry.REGISTRY_KEY;
    }


    /**
     * Can be used to load the {@link SkillStorage} from an {@link Entity}.
     *
     * @see SkillStorage
     */
    @NotNull
    public static SkillStorage getSkillsFrom(Entity entity) {
        return EntitySkillCapability.load(entity);
    }

    /**
     * This Method filters {@link ManasSkill} that meets the conditions of the {@link SkillActivationEvent} then send packet for them.
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
     */
    public static void sendSkillTogglePacket() {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        List<ResourceLocation> packetSkills = new ArrayList<>();

        for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
            SkillToggleEvent event = new SkillToggleEvent(skillInstance, player);
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            packetSkills.add(skillInstance.getSkillId());
        }

        if (packetSkills.isEmpty()) return;
        ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillTogglePacket(packetSkills));
    }
}