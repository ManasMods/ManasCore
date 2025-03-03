/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.config.imp.network;

import io.github.manasmods.manascore.config.imp.network.s2c.SyncConfigToClientPayload;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;

public class ManasConfigNetwork {
    public static void init() {
        NetworkUtils.registerS2CPayload(SyncConfigToClientPayload.TYPE,
                SyncConfigToClientPayload.STREAM_CODEC, SyncConfigToClientPayload::handle);
    }
}
