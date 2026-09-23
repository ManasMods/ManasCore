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
        TeamEvents.CLIENT_INVITES_UPDATED.register((incoming, outgoing) -> LOG.info("[team-client] invites updated incoming={} outgoing={}", incoming, outgoing));
        TeamEvents.CLIENT_ACTION_RESULT.register((action, teamId, result) -> LOG.info("[team-client] action result {} {} {}", action, teamId, result));
        TeamEvents.CLIENT_RELATIONS_UPDATED.register((owner, type) -> LOG.info("[team-client] relations updated {} {}", owner.getName().getString(), type.getId()));
        TeamEvents.CLIENT_INVITABLE_UPDATED.register((teamId, players) -> LOG.info("[team-client] invitable {} {}", teamId, players));
    }
}
