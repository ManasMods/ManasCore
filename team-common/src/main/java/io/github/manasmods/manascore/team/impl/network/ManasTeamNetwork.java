/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;
import io.github.manasmods.manascore.team.impl.client.ManasCoreTeamClient;
import io.github.manasmods.manascore.team.impl.network.c2s.TeamActionPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.RemoveTeamPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitablePayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncNamesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncTeamPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.TeamActionResultPayload;

/**
 * Registers the payloads of the team module and the server side handler for client requests.
 */
public class ManasTeamNetwork {
    /**
     * Used to register every s2c and c2s payload of the team module.
     * The client side wiring is only set up on the client.
     */
    public static void init() {
        NetworkUtils.registerS2CPayload(SyncTeamPayload.TYPE, SyncTeamPayload.STREAM_CODEC, SyncTeamPayload::handle);
        NetworkUtils.registerS2CPayload(RemoveTeamPayload.TYPE, RemoveTeamPayload.STREAM_CODEC, RemoveTeamPayload::handle);
        NetworkUtils.registerS2CPayload(SyncInvitesPayload.TYPE, SyncInvitesPayload.STREAM_CODEC, SyncInvitesPayload::handle);
        NetworkUtils.registerS2CPayload(SyncInvitablePayload.TYPE, SyncInvitablePayload.STREAM_CODEC, SyncInvitablePayload::handle);
        NetworkUtils.registerS2CPayload(SyncNamesPayload.TYPE, SyncNamesPayload.STREAM_CODEC, SyncNamesPayload::handle);
        NetworkUtils.registerS2CPayload(TeamActionResultPayload.TYPE, TeamActionResultPayload.STREAM_CODEC, TeamActionResultPayload::handle);
        NetworkUtils.registerC2SPayload(TeamActionPayload.TYPE, TeamActionPayload.STREAM_CODEC, TeamActionPayload::handle);
        TeamActionHandler.init();
        if (Platform.getEnvironment() == Env.CLIENT) ManasCoreTeamClient.init();
    }
}
