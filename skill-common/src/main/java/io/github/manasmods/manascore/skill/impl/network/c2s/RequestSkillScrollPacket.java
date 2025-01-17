/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RequestSkillScrollPacket(
        double delta,
        List<ResourceLocation> skillList
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillScrollPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_scroll"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillScrollPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillScrollPacket::encode, RequestSkillScrollPacket::new);

    public RequestSkillScrollPacket(FriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.delta);
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation skillId : skillList) {
                storage.getSkill(skillId).ifPresent(skill -> {
                    if (!skill.canInteractSkill(player)) return;
                    skill.onScroll(player, delta, 0);
                    storage.markDirty();
                });
            }
        });
    }

    public @NotNull Type<RequestSkillScrollPacket> type() {
        return TYPE;
    }
}
