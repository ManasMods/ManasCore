/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.network.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.race.ModuleConstants;
import io.github.manasmods.manascore.race.api.ManasRaceInstance;
import io.github.manasmods.manascore.race.api.RaceAPI;
import io.github.manasmods.manascore.race.api.RaceEvents;
import io.github.manasmods.manascore.race.api.Races;
import io.github.manasmods.manascore.race.impl.RaceStorage;
import io.github.manasmods.manascore.race.impl.TickingRace;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record RequestRaceAbilityActivationPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestRaceAbilityActivationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModuleConstants.MOD_ID, "request_race_ability_activation"));
    public static final StreamCodec<FriendlyByteBuf, RequestRaceAbilityActivationPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestRaceAbilityActivationPacket::encode, RequestRaceAbilityActivationPacket::new);

    public RequestRaceAbilityActivationPacket(FriendlyByteBuf buf) {
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
            Optional<ManasRaceInstance> optional = storage.getRace();
            if (optional.isEmpty()) return;

            ManasRaceInstance instance = optional.get();
            if (RaceEvents.ACTIVATE_ABILITY.invoker().activateAbility(instance, player).isFalse()) return;
            if (!instance.canActivateAbility(player)) return;
            if (instance.isOnCooldown()) return;

            instance.onActivateAbility(player);
            RaceStorage.tickingRaces.put(player.getUUID(), new TickingRace());
            storage.checkAndMarkDirty(instance);
        });
    }

    public @NotNull Type<RequestRaceAbilityActivationPacket> type() {
        return TYPE;
    }
}
