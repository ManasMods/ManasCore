package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillReleaseEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSkillReleasePacket {
    private final int heldTick;
    private final int keyNumber;
    public RequestSkillReleasePacket(FriendlyByteBuf buf) {
        this.keyNumber = buf.readInt();
        this.heldTick = buf.readInt();
    }

    public RequestSkillReleasePacket(int keyNumber, int ticks) {
        this.keyNumber = keyNumber;
        this.heldTick = ticks;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.keyNumber);
        buf.writeInt(this.heldTick);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                for (ManasSkillInstance skillInstance : storage.getLearnedSkills()) {

                    SkillReleaseEvent event = new SkillReleaseEvent(skillInstance, player, this.keyNumber, this.heldTick);
                    if (MinecraftForge.EVENT_BUS.post(event)) continue;

                    if (!skillInstance.canInteractSkill(player)) continue;
                    if (skillInstance.onCoolDown()) continue;

                    skillInstance.onRelease(player, event.getTicks());
                }
                storage.syncChanges();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
