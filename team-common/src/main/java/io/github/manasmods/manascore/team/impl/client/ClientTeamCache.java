/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.impl.client;

import io.github.manasmods.manascore.team.api.*;
import lombok.Setter;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;


/**
 * Client side cache of the data synced by the server: the teams of the player, their invites, last known names and invitable players.
 * <p>
 * It is filled by the s2c payloads and cleared when the player leaves the world.
 */
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

    /**
     * Used to get a cached team. The cache only holds the teams of the local player.
     */
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

    /**
     * Used to get the cached pending invites of a team.
     */
    public static List<TeamInvite> getOutgoingInvites(UUID teamId) {
        List<TeamInvite> result = new ArrayList<>();
        for (TeamInvite invite : OUTGOING) {
            if (invite.teamId().equals(teamId)) result.add(invite);
        }
        return result;
    }

    /**
     * Used to get the cached pending invites of every team of the local player.
     */
    public static List<TeamInvite> getOutgoingInvites() {
        return Collections.unmodifiableList(OUTGOING);
    }

    public static void setInvitable(UUID teamId, List<UUID> players) {
        INVITABLE.put(teamId, new ArrayList<>(players));
    }

    /**
     * Used to get the last received list of players that the team may still invite.
     */
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

    /**
     * Used to get the {@link MemberInfo} of an id.
     * Online players are resolved through the connection, other members by their last known name.
     */
    public static Optional<MemberInfo> getMemberInfo(UUID id) {
        Optional<MemberInfo> online = onlineResolver.apply(id);
        if (online.isPresent()) return online;
        return getName(id).map(name -> new MemberInfo(id, Component.literal(name), false));
    }

    /**
     * Used to queue a callback until {@link #drainPending()} runs.
     */
    public static void defer(Runnable r) {
        PENDING.add(r);
    }

    /**
     * Used to run and clear the queued callbacks.
     * It is called at the end of every client tick.
     */
    public static void drainPending() {
        if (PENDING.isEmpty()) return;
        List<Runnable> toRun = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Runnable r : toRun) r.run();
    }

    /**
     * Used to clear every cached value, called when the player leaves the world.
     */
    public static void clear() {
        TEAMS.clear();
        INVITES.clear();
        OUTGOING.clear();
        NAMES.clear();
        INVITABLE.clear();
        PENDING.clear();
    }
}
