/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.impl.network.c2s.RequestSkillScrollPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class ManasCoreSkillClient {
    public static void init() {
        ClientRawInputEvent.MOUSE_SCROLLED.register((client, amountX, amountY) -> {
            Player player = client.player;
            if (player == null) return EventResult.pass();

            Map<ResourceLocation, Integer> packetSkills = new HashMap<>();
            for (ManasSkillInstance skillInstance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
                Changeable<Integer> mode = Changeable.of(0);
                if (SkillEvents.SKILL_SCROLL_CLIENT.invoker().scroll(skillInstance, player, mode, amountY).isFalse()) continue;
                if (!skillInstance.canScroll(player, mode.get())) continue;
                packetSkills.put(skillInstance.getSkillId(), mode.get());
            }

            if (!packetSkills.isEmpty()) {
                NetworkManager.sendToServer(new RequestSkillScrollPacket(amountY, packetSkills));
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
    }
}
