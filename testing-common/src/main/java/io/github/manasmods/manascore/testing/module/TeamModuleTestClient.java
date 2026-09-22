/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.team.api.template.TeamEvents;

import static io.github.manasmods.manascore.testing.ManasCoreTesting.LOG;

public class TeamModuleTestClient {
    public static void init() {
        TeamEvents.CLIENT_TEAM_UPDATED.register(team -> LOG.info("[team-client] updated {}", team.getId()));
        TeamEvents.CLIENT_TEAM_REMOVED.register(teamId -> LOG.info("[team-client] removed {}", teamId));
        TeamEvents.CLIENT_INVITES_UPDATED.register(invites -> LOG.info("[team-client] invites updated {}", invites));
    }
}
