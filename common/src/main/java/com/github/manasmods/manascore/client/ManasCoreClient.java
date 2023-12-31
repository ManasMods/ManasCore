package com.github.manasmods.manascore.client;

import com.github.manasmods.manascore.api.skill.ManasSkillInstance;
import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.SkillEvents;
import com.github.manasmods.manascore.client.keybinding.KeybindingManager;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.network.toserver.RequestSkillScrollPacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ManasCoreClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> KeybindingManager.init());
        // Copy storage from old player to new player
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((oldPlayer, newPlayer) -> newPlayer.manasCore$setCombinedStorage(oldPlayer.manasCore$getCombinedStorage()));
        ClientRawInputEvent.MOUSE_SCROLLED.register((client, amountX, amountY) -> {
            Player player = client.player;
            if (player == null) return EventResult.pass();

            List<ResourceLocation> packetSkills = new ArrayList<>();

            for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
                if (SkillEvents.SKILL_SCROLL.invoker().scroll(skillInstance, player, amountY).isFalse()) continue;
                packetSkills.add(skillInstance.getSkillId());
            }

            if (!packetSkills.isEmpty()) {
                NetworkManager.CHANNEL.sendToServer(new RequestSkillScrollPacket(packetSkills, amountY));
                return EventResult.interruptTrue();
            }

            return EventResult.pass();
        });
    }
}
