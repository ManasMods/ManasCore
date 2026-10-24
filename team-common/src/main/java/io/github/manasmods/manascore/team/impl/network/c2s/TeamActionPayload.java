/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.team.ModuleConstants;
import io.github.manasmods.manascore.team.api.template.TeamAction;
import io.github.manasmods.manascore.team.impl.network.TeamActionHandler;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Sent from a client to the server to request a team mutation.
 * The payload only carries intent, the server validates everything again in {@link TeamActionHandler}.
 * <p>
 * The payload is handled on the server only.
 *
 * @param action Requested {@link TeamAction}.
 * @param typeId Registry id of the {@code TeamType}, used by the actions that need one.
 * @param teamId Id of the affected team, if the action needs one.
 * @param target Id of the affected entity, if the action needs one.
 * @param name   Requested team name, if the action needs one.
 */
public record TeamActionPayload(TeamAction action, @Nullable ResourceLocation typeId, @Nullable UUID teamId,
                                @Nullable UUID target, @Nullable String name) implements CustomPacketPayload {
    public static final Type<TeamActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "team_action"));
    public static final StreamCodec<FriendlyByteBuf, TeamActionPayload> STREAM_CODEC = CustomPacketPayload.codec(TeamActionPayload::encode, TeamActionPayload::new);

    public TeamActionPayload(FriendlyByteBuf buf) {
        this(readAction(buf), buf.readNullable(FriendlyByteBuf::readResourceLocation),
                buf.readNullable(b -> b.readUUID()), buf.readNullable(b -> b.readUUID()),
                buf.readNullable(b -> b.readUtf(256)));
    }

    private static TeamAction readAction(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        if (ordinal < 0 || ordinal >= TeamAction.values().length) throw new DecoderException("Unknown team action " + ordinal);
        return TeamAction.values()[ordinal];
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeNullable(this.typeId, FriendlyByteBuf::writeResourceLocation);
        buf.writeNullable(this.teamId, (b, v) -> b.writeUUID(v));
        buf.writeNullable(this.target, (b, v) -> b.writeUUID(v));
        buf.writeNullable(this.name, (b, v) -> b.writeUtf(v, 256));
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> TeamActionHandler.handle(context.getPlayer(), this));
    }

    public @NotNull Type<TeamActionPayload> type() {
        return TYPE;
    }
}
