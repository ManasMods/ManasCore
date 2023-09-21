package com.github.manasmods.manascore.capability.skill.event;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillScrollEvent;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.network.toserver.RequestSkillScrollPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ManasCore.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventListenerHandler {

    @SubscribeEvent
    public static void clientMouseScrolled(InputEvent.MouseScrollingEvent event) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        SkillStorage storage = SkillAPI.getSkillsFrom(player);
        for (ManasSkillInstance skillInstance : storage.getLearnedSkills()) {
            SkillScrollEvent scrollEvent = new SkillScrollEvent(skillInstance, player, event.getScrollDelta());
            if (MinecraftForge.EVENT_BUS.post(scrollEvent)) continue;

            ManasCoreNetwork.INSTANCE.sendToServer(new RequestSkillScrollPacket(skillInstance, event.getScrollDelta()));
        }
    }
}