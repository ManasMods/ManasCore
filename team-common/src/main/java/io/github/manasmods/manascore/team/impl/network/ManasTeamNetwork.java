/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;
import io.github.manasmods.manascore.team.impl.client.ManasCoreTeamClient;
import io.github.manasmods.manascore.team.impl.network.s2c.RemoveTeamPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncTeamPayload;

public class ManasTeamNetwork {
    public static void init() {
        NetworkUtils.registerS2CPayload(SyncTeamPayload.TYPE, SyncTeamPayload.STREAM_CODEC, SyncTeamPayload::handle);
        NetworkUtils.registerS2CPayload(RemoveTeamPayload.TYPE, RemoveTeamPayload.STREAM_CODEC, RemoveTeamPayload::handle);
        NetworkUtils.registerS2CPayload(SyncInvitesPayload.TYPE, SyncInvitesPayload.STREAM_CODEC, SyncInvitesPayload::handle);
        if (Platform.getEnvironment() == Env.CLIENT) ManasCoreTeamClient.init();
    }
}
