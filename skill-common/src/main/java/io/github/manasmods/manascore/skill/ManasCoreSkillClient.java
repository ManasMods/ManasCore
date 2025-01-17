/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillScrollPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ManasCoreSkillClient {
    public static void init() {
        ClientRawInputEvent.MOUSE_SCROLLED.register((client, amountX, amountY) -> {
            Player player = client.player;
            if (player == null) return EventResult.pass();

            List<ResourceLocation> packetSkills = new ArrayList<>();
            for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
                if (SkillEvents.SKILL_SCROLL.invoker().scroll(skillInstance, player, amountY).isFalse()) continue;
                packetSkills.add(skillInstance.getSkillId());
            }

            if (!packetSkills.isEmpty()) {
                NetworkManager.sendToServer(new RequestSkillScrollPacket(amountY, packetSkills));
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }
}
