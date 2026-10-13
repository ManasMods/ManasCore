/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.impl.network;

import io.github.manasmods.manascore.attribute.impl.network.c2s.RequestGlideStartPacket;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;

public class ManasCoreAttributeNetwork {
    public static void init() {
        NetworkUtils.registerC2SPayload(RequestGlideStartPacket.TYPE, RequestGlideStartPacket.STREAM_CODEC, RequestGlideStartPacket::handle);
    }
}
