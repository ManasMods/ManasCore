/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
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
 * An empty {@code animation} resets any active animation.
 * <p>
 * When {@code animation} finishes (non looping), a non empty {@code nextAnimation} is played automatically,
 * which allows chains such as an intro followed by a looping hold.
 *
 * @param entityId      Id of the {@link net.minecraft.world.entity.Entity} playing the animation.
 * @param animation     Animation key ({@code <modid>:<name>}). An empty string resets the active animation.
 * @param nextAnimation Animation key that plays automatically once {@code animation} finishes. Empty for none.
 * @param override      true restarts the animation from the beginning.
 * @param firstPerson   true shows the animation from the first person view of the owner.
 */
public record PlayPlayerAnimationPayload(int entityId, String animation, String nextAnimation, boolean override, boolean firstPerson) implements CustomPacketPayload {
    public static final Type<PlayPlayerAnimationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "play_player_animation"));
    public static final StreamCodec<FriendlyByteBuf, PlayPlayerAnimationPayload> STREAM_CODEC = CustomPacketPayload.codec(PlayPlayerAnimationPayload::encode, PlayPlayerAnimationPayload::new);

    /**
     * Used to read the payload from the buffer.
     *
     * @see #encode(FriendlyByteBuf)
     */
    public PlayPlayerAnimationPayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean());
    }

    /**
     * Used to write the payload to the buffer.
     *
     * @see #PlayPlayerAnimationPayload(FriendlyByteBuf)
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(animation);
        buf.writeUtf(nextAnimation);
        buf.writeBoolean(override);
        buf.writeBoolean(firstPerson);
    }

    /**
     * Used to handle the payload on the client.
     * It is applied on the main thread through {@link AnimationClientHandler} and ignored on any other side.
     */
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> AnimationClientHandler.handle(this));
    }

    public Type<PlayPlayerAnimationPayload> type() {
        return TYPE;
    }
}
