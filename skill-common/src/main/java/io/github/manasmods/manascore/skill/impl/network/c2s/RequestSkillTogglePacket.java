/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RequestSkillTogglePacket(
        ResourceLocation skillId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillTogglePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_toggle"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillTogglePacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillTogglePacket::encode, RequestSkillTogglePacket::new);

    public RequestSkillTogglePacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.skillId);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            storage.getSkill(skillId).ifPresent(skillInstance -> {
                Changeable<ManasSkillInstance> changeable = Changeable.of(skillInstance);
                if (SkillEvents.TOGGLE_SKILL.invoker().toggleSkill(changeable, player).isFalse()) return;

                ManasSkillInstance skill = changeable.get();
                if (skill == null) return;
                if (!skill.canInteractSkill(player)) return;
                if (!skill.canBeToggled(player)) return;

                if (skill.isToggled()) {
                    skill.setToggled(false);
                    skill.onToggleOff(player);
                } else {
                    skill.setToggled(true);
                    skill.onToggleOn(player);
                }
                storage.checkAndMarkDirty(skill);
            });
        });
    }

    public @NotNull Type<RequestSkillTogglePacket> type() {
        return TYPE;
    }
}
