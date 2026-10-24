/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.network;

import io.github.manasmods.manascore.network.api.util.NetworkUtils;

/**
 * This is the network setup of the animation module.
 */
public class ManasAnimationNetwork {
    /**
     * Used to register the server to client {@link PlayPlayerAnimationPayload}.
     */
    public static void init() {
        NetworkUtils.registerS2CPayload(PlayPlayerAnimationPayload.TYPE, PlayPlayerAnimationPayload.STREAM_CODEC, PlayPlayerAnimationPayload::handle);
    }
}
