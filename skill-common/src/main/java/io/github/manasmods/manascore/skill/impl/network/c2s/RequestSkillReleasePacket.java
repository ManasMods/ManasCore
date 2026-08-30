/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.skill.impl.SkillStorage;
import io.github.manasmods.manascore.skill.ModuleConstants;
import io.github.manasmods.manascore.storage.impl.StorageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RequestSkillReleasePacket(
        int keyNumber,
        int mode,
        ResourceLocation skillId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSkillReleasePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_skill_release"));
    public static final StreamCodec<FriendlyByteBuf, RequestSkillReleasePacket> STREAM_CODEC = CustomPacketPayload.codec(RequestSkillReleasePacket::encode, RequestSkillReleasePacket::new);

    public RequestSkillReleasePacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.keyNumber);
        buf.writeInt(this.mode);
        buf.writeResourceLocation(this.skillId);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;
            SkillStorage storage = StorageManager.getStorage(player, SkillStorage.getKey());
            if (storage == null) return;
            storage.getSkill(skillId).ifPresent(skillInstance -> storage.handleSkillRelease(skillInstance, keyNumber, mode, false));
        });
    }

    public @NotNull Type<RequestSkillReleasePacket> type() {
        return TYPE;
    }
}
