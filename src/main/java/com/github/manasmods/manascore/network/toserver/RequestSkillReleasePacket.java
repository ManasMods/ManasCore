package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.TickingSkill;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillReleaseEvent;
import com.github.manasmods.manascore.capability.skill.event.TickEventListenerHandler;
import com.google.common.collect.Multimap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestSkillReleasePacket {
    private final int heldTick;
    private final int keyNumber;
    private final ResourceLocation skill;
    public RequestSkillReleasePacket(FriendlyByteBuf buf) {
        this.skill = buf.readResourceLocation();
        this.keyNumber = buf.readInt();
        this.heldTick = buf.readInt();
    }

    public RequestSkillReleasePacket(ResourceLocation skill, int keyNumber, int ticks) {
        this.skill = skill;
        this.keyNumber = keyNumber;
        this.heldTick = ticks;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.skill);
        buf.writeInt(this.keyNumber);
        buf.writeInt(this.heldTick);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SkillStorage storage = SkillAPI.getSkillsFrom(player);
                ManasSkill manasSkill = SkillAPI.getSkillRegistry().getValue(this.skill);
                if (manasSkill != null) {

                    Optional<ManasSkillInstance> optional = storage.getSkill(manasSkill);
                    if (optional.isPresent()) {
                        ManasSkillInstance skillInstance = optional.get();
                        SkillReleaseEvent event = new SkillReleaseEvent(skillInstance, player, keyNumber, this.heldTick);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {

                            if (skillInstance.canInteractSkill(player)) {
                                if (!skillInstance.onCoolDown() || skillInstance.canIgnoreCoolDown(player)) {
                                    skillInstance.onRelease(player, this.heldTick);
                                }
                            }

                            skillInstance.removeHeldAttributeModifiers(player);
                            Multimap<UUID, TickingSkill> multimap = TickEventListenerHandler.tickingSkills;
                            if (multimap.containsKey(player.getUUID()))
                                multimap.get(player.getUUID()).removeIf(tickingSkill -> tickingSkill.getSkill() == skillInstance.getSkill());
                            storage.syncChanges();
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
