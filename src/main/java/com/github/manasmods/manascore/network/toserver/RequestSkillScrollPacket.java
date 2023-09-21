package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSkillScrollPacket {
    private final double delta;
    private final CompoundTag nbt;
    public RequestSkillScrollPacket(FriendlyByteBuf buf) {
        this.delta = buf.readDouble();
        this.nbt = buf.readNbt();
    }

    public RequestSkillScrollPacket(ManasSkillInstance skillInstance, double delta) {
        this.delta = delta;
        this.nbt = skillInstance.toNBT();

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(this.delta);
        buf.writeNbt(this.nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                ManasSkillInstance skillInstance = ManasSkillInstance.fromNBT(this.nbt);

                skillInstance.onScroll(player, delta < 0 ? -1 : 1);
                storage.updateSkill(skillInstance);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
