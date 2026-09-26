/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.network;

import io.github.manasmods.manascore.network.api.util.NetworkUtils;

public class ManasAnimationNetwork {
    public static void init() {
        NetworkUtils.registerS2CPayload(PlayPlayerAnimationPayload.TYPE, PlayPlayerAnimationPayload.STREAM_CODEC, PlayPlayerAnimationPayload::handle);
    }
}
