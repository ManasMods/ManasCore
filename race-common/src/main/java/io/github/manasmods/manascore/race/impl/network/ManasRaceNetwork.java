/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.impl.network;

import io.github.manasmods.manascore.network.api.util.NetworkUtils;
import io.github.manasmods.manascore.race.impl.network.c2s.RequestRaceAbilityActivationPacket;
import io.github.manasmods.manascore.race.impl.network.c2s.RequestRaceEvolutionPacket;

public class ManasRaceNetwork {
    public static void init() {
        NetworkUtils.registerC2SPayload(RequestRaceAbilityActivationPacket.TYPE,
                RequestRaceAbilityActivationPacket.STREAM_CODEC, RequestRaceAbilityActivationPacket::handle);
        NetworkUtils.registerC2SPayload(RequestRaceEvolutionPacket.TYPE,
                RequestRaceEvolutionPacket.STREAM_CODEC, RequestRaceEvolutionPacket::handle);
    }
}
