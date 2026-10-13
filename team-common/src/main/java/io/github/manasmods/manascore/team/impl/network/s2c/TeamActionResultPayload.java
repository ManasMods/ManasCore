/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.team.ModuleConstants;
import io.github.manasmods.manascore.team.api.template.TeamAction;
import io.github.manasmods.manascore.team.api.template.TeamResult;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Sent from the server to a client as the answer to a {@code TeamActionPayload}.
 * <p>
 * The payload is handled on the client only.
 *
 * @param action Requested {@link TeamAction}.
 * @param teamId Id of the affected team, null if there is none.
 * @param result {@link TeamResult} of the request.
 */
public record TeamActionResultPayload(TeamAction action, @Nullable UUID teamId, TeamResult result) implements CustomPacketPayload {
    public static final Type<TeamActionResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "team_action_result"));
    public static final StreamCodec<FriendlyByteBuf, TeamActionResultPayload> STREAM_CODEC = CustomPacketPayload.codec(TeamActionResultPayload::encode, TeamActionResultPayload::new);

    public TeamActionResultPayload(FriendlyByteBuf buf) {
        this(readAction(buf), buf.readNullable(b -> b.readUUID()), readResult(buf));
    }

    private static TeamAction readAction(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        if (ordinal < 0 || ordinal >= TeamAction.values().length) throw new DecoderException("Unknown team action " + ordinal);
        return TeamAction.values()[ordinal];
    }

    private static TeamResult readResult(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        if (ordinal < 0 || ordinal >= TeamResult.values().length) throw new DecoderException("Unknown team result " + ordinal);
        return TeamResult.values()[ordinal];
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeNullable(this.teamId, (b, v) -> b.writeUUID(v));
        buf.writeEnum(this.result);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.CLIENT) return;
        context.queue(() -> ClientAccess.handle(this));
    }

    public @NotNull Type<TeamActionResultPayload> type() {
        return TYPE;
    }
}
