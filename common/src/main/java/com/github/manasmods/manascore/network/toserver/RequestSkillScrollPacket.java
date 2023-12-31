package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.skill.SkillStorage;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
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

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
        buf.writeDouble(this.delta);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;
            SkillStorage storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation skillId : skillList) {
                storage.getSkill(skillId).ifPresent(skill -> {
                    if (!skill.canInteractSkill(player)) return;
                    skill.onScroll(player, delta);
                    storage.markDirty();
                });
            }
        });
    }
}
