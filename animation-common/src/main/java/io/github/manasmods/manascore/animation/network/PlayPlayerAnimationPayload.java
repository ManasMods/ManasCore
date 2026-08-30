/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.animation.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Tells tracking clients to play a player animation on the given entity.
 * An empty {@code animation} string resets any active animation.
 * When {@code animation} finishes (non-looping), a non-empty {@code nextAnimation} is played automatically,
 * enabling chains such as intro → looping hold.
 */
public record PlayPlayerAnimationPayload(int entityId, String animation, String nextAnimation, boolean override, boolean firstPerson) implements CustomPacketPayload {
    public static final Type<PlayPlayerAnimationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "play_player_animation"));
    public static final StreamCodec<FriendlyByteBuf, PlayPlayerAnimationPayload> STREAM_CODEC = CustomPacketPayload.codec(PlayPlayerAnimationPayload::encode, PlayPlayerAnimationPayload::new);

    public PlayPlayerAnimationPayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(animation);
        buf.writeUtf(nextAnimation);
        buf.writeBoolean(override);
        buf.writeBoolean(firstPerson);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> AnimationClientHandler.handle(this));
    }

    public Type<PlayPlayerAnimationPayload> type() {
        return TYPE;
    }
}
