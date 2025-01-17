/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RequestSkillTogglePacket(
        List<ResourceLocation> skillList
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillTogglePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_toggle"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillTogglePacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillTogglePacket::encode, RequestSkillTogglePacket::new);

    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
        this(buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation id : this.skillList) {
                storage.getSkill(id).ifPresent(skill -> {
                    if(!skill.canInteractSkill(player)) return;

                    if(skill.isToggled()) {
                        skill.setToggled(false);
                        skill.onToggleOff(player);
                    } else {
                        skill.setToggled(true);
                        skill.onToggleOn(player);
                    }

                    storage.markDirty();
                });
            }
        });
    }

    public @NotNull Type<RequestSkillTogglePacket> type() {
        return TYPE;
    }
}
