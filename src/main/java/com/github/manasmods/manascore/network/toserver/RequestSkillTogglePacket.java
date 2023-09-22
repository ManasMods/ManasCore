package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RequestSkillTogglePacket {
    private final List<ResourceLocation> skillList;
    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
        this.skillList = buf.readList(FriendlyByteBuf::readResourceLocation);
    }

    public RequestSkillTogglePacket(List<ResourceLocation> skills) {
        this.skillList = skills;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                for (ResourceLocation id : this.skillList) {
                    ManasSkill manasSkill = SkillAPI.getSkillRegistry().getValue(id);
                    if (manasSkill == null) continue;

                    Optional<ManasSkillInstance> optional = storage.getSkill(manasSkill);
                    if (optional.isEmpty()) continue;

                    ManasSkillInstance skillInstance = optional.get();
                    if (!skillInstance.canInteractSkill(player)) continue;

                    if (skillInstance.isToggled()) {
                        skillInstance.setToggled(false);
                        skillInstance.onToggleOff(player);
                    } else {
                        skillInstance.setToggled(true);
                        skillInstance.onToggleOn(player);
                    }
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
