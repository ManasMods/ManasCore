package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillToggleEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSkillTogglePacket {
    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
    }

    public RequestSkillTogglePacket() {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                for (ManasSkillInstance skillInstance : storage.getLearnedSkills()) {
                    SkillToggleEvent event = new SkillToggleEvent(skillInstance, player, !skillInstance.isToggled());
                    if (MinecraftForge.EVENT_BUS.post(event)) continue;

                    if (!skillInstance.canInteractSkill(player)) continue;

                    if (event.isToggleOn()) {
                        skillInstance.onToggleOn(player);
                    } else {
                        skillInstance.onToggleOff(player);
                    }
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
