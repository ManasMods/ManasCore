/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.network;

import io.github.manasmods.manascore.cinematic.impl.network.c2s.PlayerLoadedPacket;
import io.github.manasmods.manascore.cinematic.impl.network.s2c.CutscenePayload;
import io.github.manasmods.manascore.cinematic.impl.network.s2c.DialoguePayload;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;

public class ManasCinematicNetwork {
    public static void init() {
        NetworkUtils.registerS2CPayload(CutscenePayload.TYPE, CutscenePayload.STREAM_CODEC, CutscenePayload::handle);
        NetworkUtils.registerS2CPayload(DialoguePayload.TYPE, DialoguePayload.STREAM_CODEC, DialoguePayload::handle);
        NetworkUtils.registerC2SPayload(PlayerLoadedPacket.TYPE, PlayerLoadedPacket.STREAM_CODEC, PlayerLoadedPacket::handle);
    }
}
