/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.cinematic.ModuleConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DialoguePayload(Component speaker, Component line, boolean instant) implements CustomPacketPayload {
    public static final Type<DialoguePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DialoguePayload> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, DialoguePayload::speaker,
            ComponentSerialization.STREAM_CODEC, DialoguePayload::line,
            ByteBufCodecs.BOOL, DialoguePayload::instant,
            DialoguePayload::new);

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public Type<DialoguePayload> type() {
        return TYPE;
    }
}
