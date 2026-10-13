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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record RequestSkillScrollPacket(
        double delta,
        Map<ResourceLocation, Integer> skillList
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillScrollPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_scroll"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillScrollPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillScrollPacket::encode, RequestSkillScrollPacket::new);

    public RequestSkillScrollPacket(FriendlyByteBuf buf) {
        this(buf.readDouble(), validateList(buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readInt)));
    }

    private static Map<ResourceLocation, Integer> validateList(Map<ResourceLocation, Integer> map) {
        int maxSize = 100;
        if (map.size() > maxSize) throw new IllegalArgumentException("Skill map exceeds maximum size of " + maxSize);
        return map;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.delta);
        buf.writeMap(this.skillList, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeInt);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;

            Skills storage = SkillAPI.getSkillsFrom(player);
            for (Map.Entry<ResourceLocation, Integer> entry : skillList.entrySet()) {
                storage.getSkill(entry.getKey()).ifPresent(skillInstance -> {

                    Changeable<ManasSkillInstance> skillChangeable = Changeable.of(skillInstance);
                    Changeable<Integer> modeChangeable = Changeable.of(entry.getValue());
                    Changeable<Double> deltaChangeable = Changeable.of(delta);
                    if (SkillEvents.SKILL_SCROLL.invoker().scroll(skillChangeable, player, modeChangeable, deltaChangeable).isFalse()) return;

                    ManasSkillInstance skill = skillChangeable.get();
                    if (skill == null || deltaChangeable.isEmpty()) return;
                    if (!skill.canScroll(player, modeChangeable.get())) return;
                    if (!skill.canActivateSkill(player, modeChangeable.get())) return;

                    skill.onScroll(player, deltaChangeable.get(), modeChangeable.get());
                    storage.checkAndMarkDirty(skill);
                });
            }
        });
    }

    public @NotNull Type<RequestSkillScrollPacket> type() {
        return TYPE;
    }
}
