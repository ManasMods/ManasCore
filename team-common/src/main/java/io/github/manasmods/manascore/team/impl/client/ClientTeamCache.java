/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl.client;

import io.github.manasmods.manascore.team.api.*;
import lombok.Setter;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;


public class ClientTeamCache {
    private static final Map<UUID, Team> TEAMS = new HashMap<>();
    private static final List<TeamInvite> INVITES = new ArrayList<>();
    private static final List<TeamInvite> OUTGOING = new ArrayList<>();
    private static final Map<UUID, String> NAMES = new HashMap<>();
    private static final Map<UUID, List<UUID>> INVITABLE = new HashMap<>();
    @Setter
    private static Function<UUID, Optional<MemberInfo>> onlineResolver = id -> Optional.empty();
    private static final List<Runnable> PENDING = new ArrayList<>();

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
        INVITABLE.remove(id);
    }

    public static void setInvites(List<TeamInvite> incoming, List<TeamInvite> outgoing) {
        INVITES.clear();
        INVITES.addAll(incoming);
        OUTGOING.clear();
        OUTGOING.addAll(outgoing);
    }

    public static List<TeamInvite> getOutgoingInvites(UUID teamId) {
        List<TeamInvite> result = new ArrayList<>();
        for (TeamInvite invite : OUTGOING) {
            if (invite.teamId().equals(teamId)) result.add(invite);
        }
        return result;
    }

    public static List<TeamInvite> getOutgoingInvites() {
        return Collections.unmodifiableList(OUTGOING);
    }

    public static void setInvitable(UUID teamId, List<UUID> players) {
        INVITABLE.put(teamId, new ArrayList<>(players));
    }

    public static List<UUID> getInvitable(UUID teamId) {
        List<UUID> players = INVITABLE.get(teamId);
        return players == null ? List.of() : Collections.unmodifiableList(players);
    }

    public static void putNames(Map<UUID, String> names) {
        NAMES.putAll(names);
    }

    public static Optional<String> getName(UUID id) {
        return Optional.ofNullable(NAMES.get(id));
    }

    public static Optional<MemberInfo> getMemberInfo(UUID id) {
        Optional<MemberInfo> online = onlineResolver.apply(id);
        if (online.isPresent()) return online;
        return getName(id).map(name -> new MemberInfo(id, Component.literal(name), false));
    }

    public static void defer(Runnable r) {
        PENDING.add(r);
    }

    public static void drainPending() {
        if (PENDING.isEmpty()) return;
        List<Runnable> toRun = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Runnable r : toRun) r.run();
    }

    public static void clear() {
        TEAMS.clear();
        INVITES.clear();
        OUTGOING.clear();
        NAMES.clear();
        INVITABLE.clear();
        PENDING.clear();
    }
}
