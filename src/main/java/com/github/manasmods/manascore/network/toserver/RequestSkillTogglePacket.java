package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillToggleEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestSkillTogglePacket {
    private final ResourceLocation skill;
    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
        this.skill = buf.readResourceLocation();
    }

    public RequestSkillTogglePacket(ResourceLocation skill) {
        this.skill = skill;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.skill);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                ManasSkill manasSkill = SkillAPI.getSkillRegistry().getValue(this.skill);
                if (manasSkill != null) {

                    Optional<ManasSkillInstance> optional = storage.getSkill(manasSkill);
                    if (optional.isPresent()) {
                        ManasSkillInstance instance = optional.get();
                        SkillToggleEvent event = new SkillToggleEvent(instance, player, !instance.isToggled());
                        if (!MinecraftForge.EVENT_BUS.post(event)) {

                            if (instance.canInteractSkill(player) && instance.canBeToggled(player)) {
                                instance.setToggled(!instance.isToggled());
                                if (instance.isToggled()) {
                                    instance.onToggleOn(player);
                                } else {
                                    instance.onToggleOff(player);
                                }
                            }
                            storage.syncChanges();
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
