package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.Skills;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Supplier;

public class RequestSkillTogglePacket {
    private final List<ResourceLocation> skillList;
    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
        this.skillList = buf.readList(FriendlyByteBuf::readResourceLocation);
    }

    public RequestSkillTogglePacket(List<ResourceLocation> skills) {
        this.skillList = skills;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation id : this.skillList) {
                storage.getSkill(id).ifPresent(skill -> {
                    if(!skill.canInteractSkill(player)) return;

                    if(skill.isToggled()) {
                        skill.setToggled(false);
                        skill.onToggleOff(player);
                    } else {
                        skill.setToggled(true);
                        skill.onToggleOn(player);
                    }

                    storage.markDirty();
                });
            }
        });
    }
}
