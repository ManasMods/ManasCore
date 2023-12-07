package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.TickingSkill;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.capability.skill.event.TickEventListenerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RequestSkillActivationPacket {
    private final int keyNumber;
    private final List<ResourceLocation> skillList;
    public RequestSkillActivationPacket(FriendlyByteBuf buf) {
        this.skillList = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.keyNumber = buf.readInt();
    }

    public RequestSkillActivationPacket(List<ResourceLocation> skills, int keyNumber) {
        this.skillList = skills;
        this.keyNumber = keyNumber;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
        buf.writeInt(this.keyNumber);
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
                    if (skillInstance.onCoolDown() && !skillInstance.canIgnoreCoolDown(player)) continue;
                    skillInstance.onPressed(player);
                    TickEventListenerHandler.tickingSkills.put(player.getUUID(), new TickingSkill(skillInstance.getSkill()));
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
