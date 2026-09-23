/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
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

public class ManasTeamNetwork {
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
