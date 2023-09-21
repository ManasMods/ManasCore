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

public class RequestSkillScrollPacket {
    private final double delta;
    private final List<ResourceLocation> skillList;
    public RequestSkillScrollPacket(FriendlyByteBuf buf) {
        this.skillList = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.delta = buf.readDouble();
    }

    public RequestSkillScrollPacket(List<ResourceLocation> skills, double delta) {
        this.skillList = skills;
        this.delta = delta;

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
        buf.writeDouble(this.delta);
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

                    if (!optional.get().canInteractSkill(player)) continue;
                    optional.get().onScroll(player, delta < 0 ? -1 : 1);
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
