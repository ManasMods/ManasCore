/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.impl.SkillStorage;
import io.github.manasmods.manascore.skill.impl.TickingSkill;
import io.github.manasmods.manascore.skill.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RequestSkillActivationPacket(
        int keyNumber,
        List<ResourceLocation> skillList
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillActivationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_activation"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillActivationPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillActivationPacket::encode, RequestSkillActivationPacket::new);

    public RequestSkillActivationPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.keyNumber);
        buf.writeCollection(this.skillList, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            for (ResourceLocation skillId : skillList) {
                storage.getSkill(skillId).ifPresent(skill -> {
                    if(!skill.canInteractSkill(player)) return;

                    int mode = keyNumber;
                    if (mode >= skill.getModes()) return;
                    if (skill.onCoolDown(mode) && !skill.canIgnoreCoolDown(player, mode)) return;

                    skill.onPressed(player, keyNumber, mode);
                    skill.addHeldAttributeModifiers(player, mode);
                    SkillStorage.tickingSkills.put(player.getUUID(), new TickingSkill(skill.getSkill(), mode));
                    storage.markDirty();
                });
            }
        });
    }

    public @NotNull Type<RequestSkillActivationPacket> type() {
        return TYPE;
    }
}
