/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.manascore.skill.api.SkillEvents;
import io.github.manasmods.manascore.skill.api.Skills;
import io.github.manasmods.manascore.skill.impl.TickingSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RequestSkillActivationPacket(
        int keyNumber,
        ResourceLocation skillId,
        int mode
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillActivationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_activation"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillActivationPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillActivationPacket::encode, RequestSkillActivationPacket::new);

    public RequestSkillActivationPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readResourceLocation(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.keyNumber);
        buf.writeResourceLocation(this.skillId);
        buf.writeInt(this.mode);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;
            Skills storage = SkillAPI.getSkillsFrom(player);
            storage.getSkill(skillId).ifPresent(skillInstance -> {
                Changeable<ManasSkillInstance> changeable = Changeable.of(skillInstance);
                if (SkillEvents.ACTIVATE_SKILL.invoker().activateSkill(changeable, player, keyNumber, mode).isFalse()) return;

                ManasSkillInstance skill = changeable.get();
                if (skill == null) return;
                if(!skill.canInteractSkill(player)) return;

                if (mode < 0 || mode >= skill.getModes()) return;
                if (skill.onCoolDown(mode) && !skill.canIgnoreCoolDown(player, mode)) return;

                skill.onPressed(player, keyNumber, mode);
                skill.addHeldAttributeModifiers(player, mode);
                TickingSkill.addTickingSkill(player, skill.getSkill(), mode, keyNumber);
                storage.checkAndMarkDirty(skill);
            });
        });
    }

    public @NotNull Type<RequestSkillActivationPacket> type() {
        return TYPE;
    }
}
