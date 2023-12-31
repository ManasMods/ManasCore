package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.skill.SkillStorage;
import com.github.manasmods.manascore.skill.TickingSkill;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
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

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
        buf.writeInt(this.keyNumber);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            SkillStorage storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation skillId : skillList) {
                storage.getSkill(skillId).ifPresent(skill -> {
                    if(!skill.canInteractSkill(player)) return;
                    if (skill.onCoolDown() && !skill.canIgnoreCoolDown(player)) return;
                    skill.onPressed(player);
                    storage.markDirty();

                    SkillStorage.tickingSkills.put(player.getUUID(), new TickingSkill(skill.getSkill()));
                });
            }
        });
    }
}
