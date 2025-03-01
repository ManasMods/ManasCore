package com.github.manasmods.manascore.network.toserver;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.TickingSkill;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.api.skills.event.SkillActivationEvent;
import com.github.manasmods.manascore.capability.skill.event.TickEventListenerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestSkillActivationPacket {
    private final int keyNumber;
    private final ResourceLocation skill;
    public RequestSkillActivationPacket(FriendlyByteBuf buf) {
        this.skill = buf.readResourceLocation();
        this.keyNumber = buf.readInt();
    }

    public RequestSkillActivationPacket(ResourceLocation skill, int keyNumber) {
        this.skill = skill;
        this.keyNumber = keyNumber;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.skill);
        buf.writeInt(this.keyNumber);
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
                        ManasSkillInstance instance = optional.get();
                        SkillActivationEvent event = new SkillActivationEvent(instance, player, keyNumber);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {

                            if (instance.canInteractSkill(player)) {
                                if (!instance.onCoolDown() || instance.canIgnoreCoolDown(player)) {
                                    instance.onPressed(player);
                                    instance.addHeldAttributeModifiers(player);
                                    TickEventListenerHandler.tickingSkills.put(player.getUUID(), new TickingSkill(instance.getSkill()));
                                    storage.syncChanges();
                                }
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
