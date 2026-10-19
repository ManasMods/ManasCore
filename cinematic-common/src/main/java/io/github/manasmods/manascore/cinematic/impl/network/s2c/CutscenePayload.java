/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.cinematic.ModuleConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record CutscenePayload(ResourceLocation id, Vec3 anchor, float yaw) implements CustomPacketPayload {
    public static final Type<CutscenePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "cutscene"));
    public static final StreamCodec<FriendlyByteBuf, CutscenePayload> STREAM_CODEC = CustomPacketPayload.codec(CutscenePayload::encode, CutscenePayload::new);

    public CutscenePayload(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readVec3(), buf.readFloat());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeVec3(this.anchor);
        buf.writeFloat(this.yaw);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public Type<CutscenePayload> type() {
        return TYPE;
    }
}
