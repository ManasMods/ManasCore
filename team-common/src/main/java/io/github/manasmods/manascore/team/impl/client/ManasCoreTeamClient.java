/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.client;

import dev.architectury.event.events.client.ClientPlayerEvent;

public class ManasCoreTeamClient {
    private ManasCoreTeamClient() {
    }

    public static void init() {
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> ClientTeamCache.clear());
    }
}
