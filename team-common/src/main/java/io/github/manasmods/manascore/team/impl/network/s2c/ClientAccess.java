/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.network.s2c;

import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.Team;
import io.github.manasmods.manascore.team.api.TeamType;
import io.github.manasmods.manascore.team.impl.TeamRegistry;
import io.github.manasmods.manascore.team.impl.TeamSavedData;
import io.github.manasmods.manascore.team.impl.client.ClientTeamCache;

class ClientAccess {
    static void handle(SyncTeamPayload packet) {
        TeamType<?> type = TeamRegistry.TEAM_TYPES.get(packet.typeId());
        if (type == null) {
            ManasCoreTeam.LOG.warn("Received team of unknown type {}", packet.typeId());
            return;
        }
        Team team = TeamSavedData.deserialize(type, packet.teamTag());
        ClientTeamCache.put(team);
    }

    static void handle(RemoveTeamPayload packet) {
        ClientTeamCache.remove(packet.teamId());
    }

    static void handle(SyncInvitesPayload packet) {
        ClientTeamCache.setInvites(packet.invites());
    }
}
