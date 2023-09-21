package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillActivationEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSkillActivationPacket {
    private final int keyNumber;
    public RequestSkillActivationPacket(FriendlyByteBuf buf) {
        this.keyNumber = buf.readInt();
    }

    public RequestSkillActivationPacket(int keyNumber) {
        this.keyNumber = keyNumber;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.keyNumber);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                for (ManasSkillInstance skillInstance : storage.getLearnedSkills()) {
                    if (MinecraftForge.EVENT_BUS.post(new SkillActivationEvent(skillInstance, player, this.keyNumber))) continue;

                    if (!skillInstance.canInteractSkill(player)) continue;
                    if (skillInstance.onCoolDown()) continue;

                    skillInstance.onActivation(player);
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
