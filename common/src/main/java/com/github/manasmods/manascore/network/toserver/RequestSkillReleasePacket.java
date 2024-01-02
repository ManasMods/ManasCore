package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skill.SkillAPI;
import com.github.manasmods.manascore.api.skill.Skills;
import com.github.manasmods.manascore.skill.SkillStorage;
import com.github.manasmods.manascore.skill.TickingSkill;
import com.google.common.collect.Multimap;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestSkillReleasePacket {
    private final int heldTick;
    private final int keyNumber;
    private final List<ResourceLocation> skillList;

    public RequestSkillReleasePacket(FriendlyByteBuf buf) {
        this.skillList = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.keyNumber = buf.readInt();
        this.heldTick = buf.readInt();
    }

    public RequestSkillReleasePacket(List<ResourceLocation> skills, int keyNumber, int ticks) {
        this.skillList = skills;
        this.keyNumber = keyNumber;
        this.heldTick = ticks;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
        buf.writeInt(this.keyNumber);
        buf.writeInt(this.heldTick);
    }

    public void handle(Supplier<PacketContext> contextSupplier) {
        PacketContext context = contextSupplier.get();
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation skillId : skillList) {
                storage.getSkill(skillId).ifPresent(skill -> {
                    if(!skill.canInteractSkill(player)) return;
                    if (skill.onCoolDown() && !skill.canIgnoreCoolDown(player)) return;
                    skill.onRelease(player, heldTick);
                    storage.markDirty();

                    Multimap<UUID, TickingSkill> multimap = SkillStorage.tickingSkills;
                    if (multimap.containsKey(player.getUUID())) {
                        multimap.get(player.getUUID()).removeIf(tickingSkill -> tickingSkill.getSkill() == skill.getSkill());
                    }
                });
            }
        });
    }
}
