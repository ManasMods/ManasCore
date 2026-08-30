/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RequestRaceEvolutionPacket(
        ResourceLocation evolution
) implements CustomPacketPayload {
    public static final Type<RequestRaceEvolutionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_race_evolution"));
    public static final StreamCodec<FriendlyByteBuf, RequestRaceEvolutionPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestRaceEvolutionPacket::encode, RequestRaceEvolutionPacket::new);

    public RequestRaceEvolutionPacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.evolution);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;

            Races storage = RaceAPI.getRaceFrom(player);
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return;

            ManasRace race = RaceAPI.getRaceRegistry().get(evolution);
            if (race == null) return;

            ManasRaceInstance instance = optional.get();
            if (!instance.getNextEvolutions(player).contains(race)) return;

            double progress = instance.getEvolutionProgress(player, race);
            if (progress < 1.0F) return;
            storage.evolveRace(race);
        });
    }

    public @NotNull Type<RequestRaceEvolutionPacket> type() {
        return TYPE;
    }
}
