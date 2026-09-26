/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.race.api.Races;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RequestRaceAbilityReleasePacket() implements CustomPacketPayload {
    public static final Type<RequestRaceAbilityReleasePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_race_ability_release"));
    public static final StreamCodec<FriendlyByteBuf, RequestRaceAbilityReleasePacket> STREAM_CODEC = CustomPacketPayload.codec(RequestRaceAbilityReleasePacket::encode, RequestRaceAbilityReleasePacket::new);

    public RequestRaceAbilityReleasePacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) return;
        context.queue(() -> {
            Player player = context.getPlayer();
            if(player == null) return;

            Races storage = RaceAPI.getRaceFrom(player);
            storage.releaseHeldAbility();
        });
    }

    public @NotNull Type<RequestRaceAbilityReleasePacket> type() {
        return TYPE;
    }
}
