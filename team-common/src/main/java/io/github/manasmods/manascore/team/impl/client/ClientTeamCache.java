/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.client;

import io.github.manasmods.manascore.team.api.Team;
import io.github.manasmods.manascore.team.api.TeamInvite;

import java.util.*;


public class ClientTeamCache {
    private static final Map<UUID, Team> TEAMS = new HashMap<>();
    private static final List<TeamInvite> INVITES = new ArrayList<>();

    private ClientTeamCache() {
    }

    public static Optional<Team> getTeam(UUID id) {
        return Optional.ofNullable(TEAMS.get(id));
    }

    public static Collection<Team> getTeams() {
        return Collections.unmodifiableCollection(TEAMS.values());
    }

    public static List<TeamInvite> getInvites() {
        return Collections.unmodifiableList(INVITES);
    }

    public static void put(Team team) {
        TEAMS.put(team.getId(), team);
    }

    public static void remove(UUID id) {
        TEAMS.remove(id);
    }

    public static void setInvites(List<TeamInvite> invites) {
        INVITES.clear();
        INVITES.addAll(invites);
    }

    public static void clear() {
        TEAMS.clear();
        INVITES.clear();
    }
}
