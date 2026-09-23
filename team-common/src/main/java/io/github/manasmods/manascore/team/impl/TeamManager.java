/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import com.mojang.authlib.GameProfile;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.*;
import io.github.manasmods.manascore.team.impl.network.s2c.RemoveTeamPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncNamesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncTeamPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns every mutation of team state and every built-in listener.
 * All public methods here are server-only.
 */
public final class TeamManager {
    private static final Set<String> CLIENT_WARNED = ConcurrentHashMap.newKeySet();

    private TeamManager() {
    }

    public static boolean isServer(LivingEntity entity, String op) {
        if (!entity.level().isClientSide()) return true;
        if (CLIENT_WARNED.add(op)) ManasCoreTeam.LOG.warn("TeamAPI.{} called on the client. Team mutations are server-only.", op);
        return false;
    }

    static MinecraftServer serverOf(LivingEntity entity) {
        return entity.getServer();
    }

    @Nullable
    static TeamStorage storageOf(LivingEntity entity) {
        return entity.manasCore$getStorage(TeamStorage.getKey());
    }

    @Nullable
    static TeamStorage storageOf(MinecraftServer server, UUID id) {
        LivingEntity entity = Team.findLoaded(server, id);
        return entity == null ? null : storageOf(entity);
    }

    @SuppressWarnings("unchecked")
    static <T extends Team> TeamType<T> typeOf(Team team) {
        return (TeamType<T>) team.getType();
    }

    @SuppressWarnings("unchecked")
    static <T extends Team> T cast(Team team) {
        return (T) team;
    }

    static ResourceLocation idOf(TeamType<?> type) {
        ResourceLocation id = type.getId();
        if (id == null) throw new IllegalStateException("TeamType used before registration: " + type.getClass().getName());
        return id;
    }

    private record CreateOutcome<T extends Team>(TeamResult result, @Nullable T team) {
    }

    private static <T extends Team> CreateOutcome<T> createTeamInternal(TeamType<T> type, LivingEntity owner) {
        if (!isServer(owner, "createTeam")) return new CreateOutcome<>(TeamResult.CLIENT_SIDE, null);
        if (type.getShape() != TeamShape.GROUP) return new CreateOutcome<>(TeamResult.WRONG_SHAPE, null);
        LivingEntity resolved = type.resolveMember(owner);
        TeamStorage storage = storageOf(resolved);
        if (storage == null) return new CreateOutcome<>(TeamResult.NOT_FOUND, null);

        MinecraftServer server = serverOf(resolved);
        List<UUID> evict = planEvictions(server, type, resolved, storage);
        if (evict == null) return new CreateOutcome<>(TeamResult.LIMIT_REACHED, null);
        if (TeamEvents.TEAM_CREATE.invoker().create(type, resolved).isFalse()) return new CreateOutcome<>(TeamResult.CANCELLED, null);
        applyEvictions(server, type, resolved, storage, evict);

        T team = type.createTeam(UUID.randomUUID(), resolved.getUUID());
        Team.Internals.setOwnerName(team, nameOf(server, resolved.getUUID()).orElse(null));
        TeamSavedData.get(server).putTeam(team);
        storage.addTeamId(idOf(type), team.getId());
        type.onTeamCreated(team);
        TeamEvents.TEAM_CREATED.invoker().run(team);
        syncTeam(server, team);
        return new CreateOutcome<>(TeamResult.ACCEPTED, team);
    }

    public static <T extends Team> TeamResult tryCreateTeam(TeamType<T> type, LivingEntity owner) {
        return createTeamInternal(type, owner).result();
    }

    public static <T extends Team> Optional<T> createTeam(TeamType<T> type, LivingEntity owner) {
        return Optional.ofNullable(createTeamInternal(type, owner).team());
    }

    public static TeamResult tryDisbandTeam(MinecraftServer server, Team team) {
        if (TeamEvents.TEAM_DISBAND.invoker().run(team).isFalse()) return TeamResult.CANCELLED;
        disbandInternal(server, team);
        return TeamResult.ACCEPTED;
    }

    public static boolean disbandTeam(MinecraftServer server, Team team) {
        return tryDisbandTeam(server, team).isAccepted();
    }

    private static void disbandInternal(MinecraftServer server, Team team) {
        TeamSavedData data = TeamSavedData.get(server);
        ResourceLocation typeId = idOf(team.getType());
        data.getInvites().removeIf(invite -> invite.teamId().equals(team.getId()));
        syncTeamInvites(server, team);

        for (UUID member : new ArrayList<>(team.getMembers())) {
            Team.Internals.removeMember(team, member);
            data.unindexMember(team.getId(), member);
            TeamStorage storage = storageOf(server, member);

            if (storage != null) storage.removeTeamId(typeId, team.getId());
            sendRemove(server, team, member);
            typeOf(team).onMemberRemoved(cast(team), member, LeaveReason.DISBAND);
            TeamEvents.MEMBER_LEFT.invoker().run(team, member, LeaveReason.DISBAND);
        }

        data.removeTeam(team.getId());
        typeOf(team).onTeamDisbanded(cast(team));
        TeamEvents.TEAM_DISBANDED.invoker().run(team);
    }

    /**
     * Under LEAVE_OLDEST the evictions are applied before the final team check, so NOT_FOUND
     * can be returned after the entity already left older teams.
     */
    public static TeamResult tryAddMember(Team team, LivingEntity entity) {
        if (!isServer(entity, "addMember")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolved = type.resolveMember(entity);
        TeamStorage storage = storageOf(resolved);
        if (storage == null) return TeamResult.NOT_FOUND;
        if (team.isMember(resolved)) return TeamResult.ALREADY_MEMBER;
        if (team.size() >= type.getMaxMembers()) return TeamResult.TEAM_FULL;
        if (!type.canJoin(team, resolved)) return TeamResult.NOT_ALLOWED;

        MinecraftServer server = serverOf(resolved);
        List<UUID> evict = planEvictions(server, type, resolved, storage);
        if (evict == null) return TeamResult.LIMIT_REACHED;
        if (TeamEvents.MEMBER_JOIN.invoker().run(team, resolved).isFalse()) return TeamResult.CANCELLED;
        applyEvictions(server, type, resolved, storage, evict);
        if (TeamSavedData.get(server).getTeam(team.getId()).isEmpty()) return TeamResult.NOT_FOUND;

        Team.Internals.addMember(team, resolved.getUUID());
        TeamSavedData.get(server).indexMember(team.getId(), resolved.getUUID());
        storage.addTeamId(idOf(type), team.getId());
        TeamSavedData.get(server).setDirty();
        type.onMemberAdded(team, resolved.getUUID());
        TeamEvents.MEMBER_JOINED.invoker().run(team, resolved);
        syncTeam(server, team);
        return TeamResult.ACCEPTED;
    }

    public static boolean addMember(Team team, LivingEntity entity) {
        return tryAddMember(team, entity).isAccepted();
    }

    /**
     * Removes a member. {@code entity} is the loaded entity when available; null for offline
     * or dead members. {@link TeamEvents#MEMBER_LEAVE} fires only for LEAVE and KICK.
     */
    public static TeamResult tryRemoveMember(MinecraftServer server, Team team, UUID memberId, @Nullable LivingEntity entity, LeaveReason reason) {
        if (!team.isMember(memberId)) return TeamResult.NOT_MEMBER;
        boolean cancellable = reason == LeaveReason.LEAVE || reason == LeaveReason.KICK;
        if (cancellable && entity != null && TeamEvents.MEMBER_LEAVE.invoker().run(team, entity, reason).isFalse()) return TeamResult.CANCELLED;

        TeamType<Team> type = typeOf(team);
        boolean wasOwner = team.isOwner(memberId);
        Team.Internals.removeMember(team, memberId);
        TeamSavedData.get(server).unindexMember(team.getId(), memberId);
        TeamStorage storage = entity != null ? storageOf(entity) : storageOf(server, memberId);
        if (storage != null) storage.removeTeamId(idOf(type), team.getId());
        sendRemove(server, team, memberId);
        type.onMemberRemoved(team, memberId, reason);
        TeamEvents.MEMBER_LEFT.invoker().run(team, memberId, reason);

        if (team.size() == 0) {
            disbandInternal(server, team);
            return TeamResult.ACCEPTED;
        }

        if (wasOwner) {
            UUID newOwner = type.pickNewOwner(team);
            if (newOwner == null || !team.isMember(newOwner)) {
                disbandInternal(server, team);
                return TeamResult.ACCEPTED;
            }
            Team.Internals.setOwner(team, newOwner);
            TeamSavedData.get(server).indexMember(team.getId(), newOwner);
            Team.Internals.setOwnerName(team, nameOf(server, newOwner).orElse(null));
            TeamEvents.OWNER_CHANGED.invoker().run(team, memberId, newOwner);
        }

        TeamSavedData.get(server).setDirty();
        syncTeam(server, team);
        return TeamResult.ACCEPTED;
    }

    public static boolean removeMember(MinecraftServer server, Team team, UUID memberId, @Nullable LivingEntity entity, LeaveReason reason) {
        return tryRemoveMember(server, team, memberId, entity, reason).isAccepted();
    }

    public static TeamResult trySetOwner(MinecraftServer server, Team team, UUID target) {
        if (!team.isMember(target)) return TeamResult.NOT_MEMBER;
        if (team.isOwner(target)) return TeamResult.UNCHANGED;

        UUID old = team.getOwner();
        Team.Internals.setOwner(team, target);
        TeamSavedData.get(server).indexMember(team.getId(), target);
        Team.Internals.setOwnerName(team, nameOf(server, target).orElse(null));
        TeamSavedData.get(server).setDirty();
        TeamEvents.OWNER_CHANGED.invoker().run(team, old, target);
        syncTeam(server, team);
        return TeamResult.ACCEPTED;
    }

    public static TeamResult trySetOwner(Team team, LivingEntity entity) {
        if (!isServer(entity, "setOwner")) return TeamResult.CLIENT_SIDE;
        LivingEntity resolved = typeOf(team).resolveMember(entity);
        return trySetOwner(serverOf(resolved), team, resolved.getUUID());
    }

    public static boolean setOwner(Team team, LivingEntity entity) {
        return trySetOwner(team, entity).isAccepted();
    }

    public static TeamResult tryKick(Team team, LivingEntity actor, UUID target) {
        if (!isServer(actor, "kick")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedActor = type.resolveMember(actor);
        if (!type.canKick(team, resolvedActor, target)) {
            ManasCoreTeam.LOG.debug("Kick refused ({}): team {} actor {} target {}", TeamResult.NOT_ALLOWED, team.getId(), resolvedActor.getUUID(), target);
            return TeamResult.NOT_ALLOWED;
        }
        MinecraftServer server = serverOf(resolvedActor);
        return tryRemoveMember(server, team, target, Team.findLoaded(server, target), LeaveReason.KICK);
    }

    public static boolean kick(Team team, LivingEntity actor, UUID target) {
        return tryKick(team, actor, target).isAccepted();
    }

    public static TeamResult tryPromote(Team team, LivingEntity actor, UUID target) {
        if (!isServer(actor, "promote")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedActor = type.resolveMember(actor);
        if (!type.canPromote(team, resolvedActor, target)) {
            ManasCoreTeam.LOG.debug("Promote refused ({}): team {} actor {} target {}", TeamResult.NOT_ALLOWED, team.getId(), resolvedActor.getUUID(), target);
            return TeamResult.NOT_ALLOWED;
        }
        return trySetOwner(serverOf(resolvedActor), team, target);
    }

    public static boolean promote(Team team, LivingEntity actor, UUID target) {
        return tryPromote(team, actor, target).isAccepted();
    }

    public static TeamResult tryLeave(Team team, LivingEntity actor) {
        if (!isServer(actor, "leave")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedActor = type.resolveMember(actor);
        if (!type.canLeave(team, resolvedActor)) {
            ManasCoreTeam.LOG.debug("Leave refused ({}): team {} actor {}", TeamResult.NOT_ALLOWED, team.getId(), resolvedActor.getUUID());
            return TeamResult.NOT_ALLOWED;
        }
        return tryRemoveMember(serverOf(resolvedActor), team, resolvedActor.getUUID(), resolvedActor, LeaveReason.LEAVE);
    }

    public static boolean leave(Team team, LivingEntity actor) {
        return tryLeave(team, actor).isAccepted();
    }

    public static TeamResult tryDisband(Team team, LivingEntity actor) {
        if (!isServer(actor, "disband")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedActor = type.resolveMember(actor);
        if (!type.canDisband(team, resolvedActor)) {
            ManasCoreTeam.LOG.debug("Disband refused ({}): team {} actor {}", TeamResult.NOT_ALLOWED, team.getId(), resolvedActor.getUUID());
            return TeamResult.NOT_ALLOWED;
        }
        return tryDisbandTeam(serverOf(resolvedActor), team);
    }

    public static boolean disband(Team team, LivingEntity actor) {
        return tryDisband(team, actor).isAccepted();
    }

    public static TeamResult trySetTeamName(MinecraftServer server, Team team, @Nullable String name) {
        String sanitized = sanitizeName(name);
        String old = team.getName();
        if (Objects.equals(old, sanitized)) return TeamResult.UNCHANGED;
        if (sanitized != null && sanitized.length() > typeOf(team).getMaxNameLength()) return TeamResult.INVALID;

        Team.Internals.setName(team, sanitized);
        TeamSavedData.get(server).setDirty();
        TeamEvents.TEAM_RENAMED.invoker().run(team, old, sanitized);
        syncTeam(server, team);
        return TeamResult.ACCEPTED;
    }

    public static boolean setTeamName(MinecraftServer server, Team team, @Nullable String name) {
        return trySetTeamName(server, team, name).isAccepted();
    }

    public static TeamResult trySetTeamName(Team team, LivingEntity actor, @Nullable String name) {
        if (!isServer(actor, "setTeamName")) return TeamResult.CLIENT_SIDE;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolved = type.resolveMember(actor);
        if (!type.canRename(team, resolved)) return TeamResult.NOT_ALLOWED;
        return trySetTeamName(serverOf(resolved), team, name);
    }

    public static boolean setTeamName(Team team, LivingEntity actor, @Nullable String name) {
        return trySetTeamName(team, actor, name).isAccepted();
    }

    @Nullable
    private static String sanitizeName(@Nullable String name) {
        if (name == null) return null;
        String sanitized = name.replaceAll("(?s)§.?", "").replaceAll("\\p{Cntrl}", "").trim();
        return sanitized.isBlank() ? null : sanitized;
    }

    /**
     * Dry run for {@link TeamType#getMaxTeamsPerMember()} enforcement before a join. Mutates
     * nothing. {@code storage}'s ids for this type are split into stale ones (the team no
     * longer exists, or exists but no longer contains {@code entity} — e.g. a dangling id left
     * behind by an out-of-band removal) and valid ones (team exists and contains the entity),
     * preserving join order.
     * <p>Returns the ids that must be pruned/evicted before the join may proceed: every stale
     * id, plus — only when the valid count is already at or above the limit — either
     * {@code null} (the join must be rejected, under {@link LimitPolicy#REJECT}) or the oldest
     * surplus valid ids (under {@link LimitPolicy#LEAVE_OLDEST}). When {@code null} is returned
     * nothing (not even the stale ids) has been decided to be removed, since the caller is
     * expected to abort entirely.
     * <p>A non-positive {@code maxTeamsPerMember()} always rejects.
     */
    @Nullable
    private static List<UUID> planEvictions(MinecraftServer server, TeamType<?> type, LivingEntity entity, TeamStorage storage) {
        TeamSavedData data = TeamSavedData.get(server);
        List<UUID> stale = new ArrayList<>();
        List<UUID> valid = new ArrayList<>();
        for (UUID id : storage.getTeamIds(type)) {
            Optional<Team> team = data.getTeam(id);
            if (team.isEmpty() || !team.get().isMember(entity)) {
                stale.add(id);
            } else {
                valid.add(id);
            }
        }

        int max = type.getMaxTeamsPerMember();
        if (max <= 0) return null;
        if (valid.size() < max) return stale;
        if (type.onLimitReached() == LimitPolicy.REJECT) return null;

        int surplus = Math.min(valid.size(), valid.size() - max + 1);
        List<UUID> result = new ArrayList<>(stale);
        result.addAll(valid.subList(0, surplus));
        return result;
    }

    /**
     * Applies the plan produced by {@link #planEvictions}. A stale id (team missing, or no
     * longer containing {@code entity}) is pruned straight from storage. A still-valid id is
     * left through {@link #removeMember} with a {@code null} entity, which skips the
     * cancellable {@link TeamEvents#MEMBER_LEAVE} check (only fired when the entity is
     * non-null) — automatic evictions under {@link LimitPolicy#LEAVE_OLDEST} cannot be vetoed.
     */
    private static void applyEvictions(MinecraftServer server, TeamType<?> type, LivingEntity entity, TeamStorage storage, List<UUID> evict) {
        ResourceLocation typeId = idOf(type);
        for (UUID id : evict) {
            Optional<Team> team = TeamSavedData.get(server).getTeam(id);
            if (team.isEmpty() || !team.get().isMember(entity)) {
                storage.removeTeamId(typeId, id);
            } else {
                removeMember(server, team.get(), entity.getUUID(), null, LeaveReason.LEAVE);
            }
        }
    }

    public static void syncTeam(MinecraftServer server, Team team) {
        SyncTeamPayload payload = new SyncTeamPayload(idOf(team.getType()), TeamSavedData.serialize(team), namesFor(server, namedMembers(team)));
        for (LivingEntity member : team.getOnlineMembers(server)) {
            if (member instanceof ServerPlayer player) NetworkManager.sendToPlayer(player, payload);
        }
    }

    private static void sendRemove(MinecraftServer server, Team team, UUID memberId) {
        ServerPlayer player = server.getPlayerList().getPlayer(memberId);
        if (player != null) NetworkManager.sendToPlayer(player, new RemoveTeamPayload(team.getId()));
    }

    /** Sends every team the player belongs to. Invites are synced separately by {@link #syncInvites(MinecraftServer, UUID)}. */
    public static void syncAllFor(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        TeamSavedData data = TeamSavedData.get(server);
        for (UUID teamId : data.getTeamIdsOf(player.getUUID())) {
            data.getTeam(teamId).ifPresent(team -> {
                SyncTeamPayload payload = new SyncTeamPayload(idOf(team.getType()), TeamSavedData.serialize(team), namesFor(server, namedMembers(team)));
                NetworkManager.sendToPlayer(player, payload);
            });
        }
    }

    private static Set<UUID> namedMembers(Team team) {
        Set<UUID> ids = new LinkedHashSet<>(team.getMembers());
        ids.add(team.getOwner());
        return ids;
    }

    /** Online player's profile name, else the last-known saved name, else the profile cache. */
    public static Optional<String> nameOf(MinecraftServer server, UUID id) {
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) return Optional.of(online.getGameProfile().getName());

        Optional<String> saved = TeamSavedData.get(server).getName(id);
        if (saved.isPresent()) return saved;

        GameProfileCache cache = server.getProfileCache();
        return cache == null ? Optional.empty() : cache.get(id).map(GameProfile::getName);
    }

    public static Optional<MemberInfo> memberInfo(MinecraftServer server, UUID id) {
        return nameOf(server, id).map(name -> new MemberInfo(id, Component.literal(name), server.getPlayerList().getPlayer(id) != null));
    }

    public static Map<UUID, String> namesFor(MinecraftServer server, Collection<UUID> ids) {
        Map<UUID, String> result = new LinkedHashMap<>();
        for (UUID id : ids) nameOf(server, id).ifPresent(name -> result.put(id, name));
        return result;
    }

    /**
     * Rewrites the player's GROUP memberships from the saved team table, then migrates any
     * pre-existing RELATION data out of storage (once per player) and rebuilds storage's
     * RELATION sets from the saved table. Handles changes made while the player was offline.
     */
    public static void reconcile(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        TeamStorage storage = storageOf(player);
        if (server == null || storage == null) return;

        TeamSavedData data = TeamSavedData.get(server);
        Map<ResourceLocation, List<UUID>> byType = new HashMap<>();
        for (UUID teamId : data.getTeamIdsOf(player.getUUID())) {
            data.getTeam(teamId).ifPresent(team -> byType.computeIfAbsent(idOf(team.getType()), k -> new ArrayList<>()).add(team.getId()));
        }

        Map<ResourceLocation, Set<UUID>> existing = storage.getAllTeamIds();
        Set<ResourceLocation> allTypes = new LinkedHashSet<>(existing.keySet());
        allTypes.addAll(byType.keySet());

        for (ResourceLocation typeId : allTypes) {
            Set<UUID> saved = new LinkedHashSet<>(byType.getOrDefault(typeId, List.of()));
            List<UUID> ordered = new ArrayList<>();
            for (UUID id : existing.getOrDefault(typeId, Set.of())) {
                if (saved.contains(id)) ordered.add(id);
            }
            for (UUID id : saved) {
                if (!ordered.contains(id)) ordered.add(id);
            }
            storage.setTeamIds(typeId, ordered);
        }

        reconcileRelations(server, player.getUUID(), storage);
        syncRelationNames(server, player);
    }

    /** Sends last-known names for every relation the player has, in either direction, across every type. */
    public static void syncRelationNames(MinecraftServer server, ServerPlayer player) {
        TeamSavedData data = TeamSavedData.get(server);
        UUID uuid = player.getUUID();
        Set<UUID> ids = new LinkedHashSet<>();
        for (Set<UUID> related : data.getAllRelated(uuid).values()) ids.addAll(related);
        for (Set<UUID> related : data.getAllRelatedBy(uuid).values()) ids.addAll(related);
        if (ids.isEmpty()) return;
        sendNames(server, player, ids);
    }

    /**
     * Migrates {@code storage}'s pre-existing RELATION data into {@link TeamSavedData} (once
     * per entity), then rebuilds {@code storage}'s RELATION sets from the saved table.
     */
    private static void reconcileRelations(MinecraftServer server, UUID uuid, TeamStorage storage) {
        TeamSavedData data = TeamSavedData.get(server);
        if (!data.isRelationsMigrated(uuid)) {
            for (Map.Entry<ResourceLocation, Set<UUID>> entry : storage.getAllRelated().entrySet()) {
                ResourceLocation typeId = entry.getKey();
                for (UUID related : entry.getValue()) {
                    TeamType<?> type = TeamRegistry.TEAM_TYPES.get(typeId);
                    if (type != null && type.isSymmetricRelation() && data.isRelationsMigrated(related) && !data.getRelated(typeId, related).contains(uuid)) continue;
                    data.addRelated(typeId, uuid, related);
                }
            }
            data.markRelationsMigrated(uuid);
        }

        Set<ResourceLocation> relationTypes = new LinkedHashSet<>(storage.getAllRelated().keySet());
        relationTypes.addAll(data.getAllRelated(uuid).keySet());
        for (ResourceLocation typeId : relationTypes) storage.setRelated(typeId, data.getRelated(typeId, uuid));

        Set<ResourceLocation> inboundTypes = new LinkedHashSet<>(storage.getAllRelatedBy().keySet());
        inboundTypes.addAll(data.getAllRelatedBy(uuid).keySet());
        for (ResourceLocation typeId : inboundTypes) storage.setInbound(typeId, data.getRelatedBy(typeId, uuid));
    }

    private record InviteOutcome(TeamResult result, @Nullable TeamInvite invite) {
    }

    private static InviteOutcome inviteInternal(Team team, LivingEntity inviter, LivingEntity invitee) {
        if (!isServer(inviter, "invite")) return new InviteOutcome(TeamResult.CLIENT_SIDE, null);
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedInviter = type.resolveMember(inviter);
        LivingEntity resolvedInvitee = type.resolveMember(invitee);
        if (team.isMember(resolvedInvitee)) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", TeamResult.ALREADY_MEMBER, team.getId(), resolvedInviter.getUUID(), resolvedInvitee.getUUID());
            return new InviteOutcome(TeamResult.ALREADY_MEMBER, null);
        }
        if (team.size() >= type.getMaxMembers()) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", TeamResult.TEAM_FULL, team.getId(), resolvedInviter.getUUID(), resolvedInvitee.getUUID());
            return new InviteOutcome(TeamResult.TEAM_FULL, null);
        }
        if (!type.canInvite(team, resolvedInviter, resolvedInvitee)) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", TeamResult.NOT_ALLOWED, team.getId(), resolvedInviter.getUUID(), resolvedInvitee.getUUID());
            return new InviteOutcome(TeamResult.NOT_ALLOWED, null);
        }
        return createInvite(serverOf(inviter), team.getId(), idOf(type), resolvedInviter, resolvedInvitee, type.getInviteTimeoutTicks());
    }

    public static TeamResult tryInvite(Team team, LivingEntity inviter, LivingEntity invitee) {
        return inviteInternal(team, inviter, invitee).result();
    }

    public static Optional<TeamInvite> invite(Team team, LivingEntity inviter, LivingEntity invitee) {
        return Optional.ofNullable(inviteInternal(team, inviter, invitee).invite());
    }

    /** Server only. Online players this team may still invite, with every limit and hook applied. */
    public static List<ServerPlayer> getInvitable(Team team, ServerPlayer requester) {
        MinecraftServer server = requester.getServer();
        TeamType<Team> type = typeOf(team);
        if (team.size() >= type.getMaxMembers()) return List.of();

        Set<UUID> pending = new HashSet<>();
        for (TeamInvite invite : TeamSavedData.get(server).getInvites()) {
            if (invite.teamId().equals(team.getId())) pending.add(invite.invitee());
        }

        LivingEntity resolvedRequester = type.resolveMember(requester);
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            LivingEntity candidate = type.resolveMember(p);
            if (candidate.getUUID().equals(resolvedRequester.getUUID())) continue;
            if (team.isMember(candidate)) continue;
            if (pending.contains(candidate.getUUID())) continue;
            if (!type.canInvite(team, resolvedRequester, candidate)) continue;
            if (!type.canJoin(team, candidate)) continue;
            TeamStorage storage = storageOf(candidate);
            if (storage == null || planEvictions(server, type, candidate, storage) == null) continue;
            result.add(p);
        }
        return result;
    }

    /** Used to get every pending invite sent by the team. */
    public static List<TeamInvite> getOutgoingInvites(MinecraftServer server, Team team) {
        List<TeamInvite> result = new ArrayList<>();
        for (TeamInvite invite : TeamSavedData.get(server).getInvites()) {
            if (invite.teamId().equals(team.getId())) result.add(invite);
        }
        return result;
    }

    /** Determine if the team already has a pending invite for the entity. */
    public static boolean hasPendingInvite(MinecraftServer server, Team team, UUID invitee) {
        return TeamSavedData.get(server).findInvite(invitee, team.getId()).isPresent();
    }

    private static InviteOutcome createInvite(MinecraftServer server, UUID teamId, ResourceLocation typeId, LivingEntity inviter, LivingEntity invitee, int timeoutTicks) {
        TeamSavedData data = TeamSavedData.get(server);
        if (data.findInvite(invitee.getUUID(), teamId).isPresent()) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", TeamResult.INVITE_PENDING, teamId, inviter.getUUID(), invitee.getUUID());
            return new InviteOutcome(TeamResult.INVITE_PENDING, null);
        }
        TeamInvite invite = new TeamInvite(teamId, typeId, inviter.getUUID(), invitee.getUUID(), server.getTickCount() + timeoutTicks);
        if (TeamEvents.INVITE_SEND.invoker().run(invite).isFalse()) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", TeamResult.CANCELLED, teamId, inviter.getUUID(), invitee.getUUID());
            return new InviteOutcome(TeamResult.CANCELLED, null);
        }

        data.getInvites().add(invite);
        syncInvites(server, invite.invitee());
        data.getTeam(teamId).ifPresent(team -> syncTeamInvites(server, team));
        return new InviteOutcome(TeamResult.ACCEPTED, invite);
    }

    /**
     * An invite of an unregistered type is removed and synced before NOT_FOUND is returned;
     * the join result (for example TEAM_FULL) is propagated as-is.
     */
    public static TeamResult tryAcceptInvite(LivingEntity invitee, UUID teamId) {
        if (!isServer(invitee, "acceptInvite")) return TeamResult.CLIENT_SIDE;
        MinecraftServer server = serverOf(invitee);
        TeamSavedData data = TeamSavedData.get(server);
        Optional<TeamInvite> optional = data.findInvite(invitee.getUUID(), teamId);
        if (optional.isEmpty()) return TeamResult.NOT_FOUND;

        TeamInvite invite = optional.get();
        TeamType<?> type = TeamRegistry.TEAM_TYPES.get(invite.typeId());
        if (type == null) {
            data.getInvites().remove(invite);
            syncInvites(server, invitee.getUUID());
            data.getTeam(invite.teamId()).ifPresent(team -> syncTeamInvites(server, team));
            return TeamResult.NOT_FOUND;
        }

        Optional<Team> team = data.getTeam(invite.teamId());
        if (team.isEmpty()) return TeamResult.NOT_FOUND;
        TeamResult joinResult = tryAddMember(team.get(), invitee);
        if (!joinResult.isAccepted()) return joinResult;

        data.getInvites().remove(invite);
        TeamEvents.INVITE_ACCEPTED.invoker().run(invite);
        syncInvites(server, invitee.getUUID());
        syncTeamInvites(server, team.get());
        return TeamResult.ACCEPTED;
    }

    /**
     * Returns false when the invite does not exist, the type is unknown or the join was rejected.
     */
    public static boolean acceptInvite(LivingEntity invitee, UUID teamId) {
        return tryAcceptInvite(invitee, teamId).isAccepted();
    }

    public static TeamResult tryDeclineInvite(LivingEntity invitee, UUID teamId) {
        if (!isServer(invitee, "declineInvite")) return TeamResult.CLIENT_SIDE;
        MinecraftServer server = serverOf(invitee);
        TeamSavedData data = TeamSavedData.get(server);
        Optional<TeamInvite> optional = data.findInvite(invitee.getUUID(), teamId);

        if (optional.isEmpty()) return TeamResult.NOT_FOUND;
        TeamInvite invite = optional.get();
        data.getInvites().remove(invite);
        TeamEvents.INVITE_DECLINED.invoker().run(invite);
        syncInvites(server, invitee.getUUID());
        data.getTeam(invite.teamId()).ifPresent(team -> syncTeamInvites(server, team));
        return TeamResult.ACCEPTED;
    }

    public static boolean declineInvite(LivingEntity invitee, UUID teamId) {
        return tryDeclineInvite(invitee, teamId).isAccepted();
    }

    public static List<TeamInvite> getPendingInvites(MinecraftServer server, UUID invitee) {
        return TeamSavedData.get(server).getInvitesFor(invitee);
    }

    public static void syncInvites(MinecraftServer server, UUID invitee) {
        ServerPlayer player = server.getPlayerList().getPlayer(invitee);
        if (player == null) return;
        TeamSavedData data = TeamSavedData.get(server);
        List<TeamInvite> incoming = data.getInvitesFor(invitee);
        Set<UUID> ownTeamIds = data.getTeamIdsOf(invitee);
        List<TeamInvite> outgoing = new ArrayList<>();
        for (TeamInvite invite : data.getInvites()) {
            if (ownTeamIds.contains(invite.teamId())) outgoing.add(invite);
        }

        Set<UUID> named = new LinkedHashSet<>();
        for (TeamInvite invite : incoming) named.add(invite.inviter());
        for (TeamInvite invite : outgoing) named.add(invite.invitee());
        NetworkManager.sendToPlayer(player, new SyncInvitesPayload(incoming, outgoing, namesFor(server, named)));
    }

    private static void syncTeamInvites(MinecraftServer server, Team team) {
        for (LivingEntity member : team.getOnlineMembers(server)) {
            if (member instanceof ServerPlayer player) syncInvites(server, player.getUUID());
        }
    }

    public static void expireInvites(MinecraftServer server) {
        TeamSavedData data = TeamSavedData.get(server);
        if (data.getInvites().isEmpty()) return;
        long tick = server.getTickCount();

        List<TeamInvite> expired = new ArrayList<>();
        for (TeamInvite invite : data.getInvites()) {
            if (invite.isExpired(tick)) expired.add(invite);
        }
        if (expired.isEmpty()) return;
        data.getInvites().removeAll(expired);

        Set<UUID> affected = new LinkedHashSet<>();
        Set<UUID> affectedTeams = new LinkedHashSet<>();
        for (TeamInvite invite : expired) {
            affected.add(invite.invitee());
            affectedTeams.add(invite.teamId());
            TeamEvents.INVITE_EXPIRED.invoker().run(invite);
        }
        for (UUID invitee : affected) syncInvites(server, invitee);
        for (UUID teamId : affectedTeams) data.getTeam(teamId).ifPresent(team -> syncTeamInvites(server, team));
    }

    public static TeamResult tryAddRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return TeamResult.WRONG_SHAPE;
        if (a.equals(b)) return TeamResult.INVALID;

        ResourceLocation typeId = idOf(type);
        TeamSavedData data = TeamSavedData.get(server);
        if (data.getRelated(typeId, a).size() >= type.getMaxRelations()) {
            ManasCoreTeam.LOG.debug("Add relation refused ({}): type {} a {} b {}", TeamResult.LIMIT_REACHED, typeId, a, b);
            return TeamResult.LIMIT_REACHED;
        }
        if (type.isSymmetricRelation() && !data.getRelated(typeId, b).contains(a) && data.getRelated(typeId, b).size() >= type.getMaxRelations()) {
            ManasCoreTeam.LOG.debug("Add relation refused ({}): type {} a {} b {}", TeamResult.LIMIT_REACHED, typeId, a, b);
            return TeamResult.LIMIT_REACHED;
        }
        if (TeamEvents.RELATION_ADD.invoker().run(type, a, b).isFalse()) return TeamResult.CANCELLED;

        boolean changed = data.addRelated(typeId, a, b);
        if (type.isSymmetricRelation()) changed |= data.addRelated(typeId, b, a);
        if (!changed) return TeamResult.UNCHANGED;

        mirrorAdd(server, typeId, a, b);
        if (type.isSymmetricRelation()) mirrorAdd(server, typeId, b, a);
        TeamEvents.RELATION_ADDED.invoker().run(type, a, b);
        return TeamResult.ACCEPTED;
    }

    public static boolean addRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        return tryAddRelation(server, type, a, b).isAccepted();
    }

    public static TeamResult tryRemoveRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return TeamResult.WRONG_SHAPE;
        if (a.equals(b)) return TeamResult.INVALID;

        ResourceLocation typeId = idOf(type);
        TeamSavedData data = TeamSavedData.get(server);
        boolean changed = data.removeRelated(typeId, a, b);
        if (type.isSymmetricRelation()) changed |= data.removeRelated(typeId, b, a);
        if (!changed) return TeamResult.UNCHANGED;

        mirrorRemove(server, typeId, a, b);
        if (type.isSymmetricRelation()) mirrorRemove(server, typeId, b, a);
        TeamEvents.RELATION_REMOVED.invoker().run(type, a, b);
        return TeamResult.ACCEPTED;
    }

    public static boolean removeRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        return tryRemoveRelation(server, type, a, b).isAccepted();
    }

    public static boolean hasRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        if (a.equals(b)) return false;
        return TeamSavedData.get(server).getRelated(idOf(type), a).contains(b);
    }

    public static Set<UUID> getRelated(MinecraftServer server, TeamType<?> type, UUID entity) {
        return TeamSavedData.get(server).getRelated(idOf(type), entity);
    }

    public static Set<UUID> getRelatedBy(MinecraftServer server, TeamType<?> type, UUID entity) {
        return TeamSavedData.get(server).getRelatedBy(idOf(type), entity);
    }

    /**
     * Adds {@code other} to {@code entity}'s loaded storage, if any, without touching the rest of
     * its set; mirrors the inbound side onto {@code other}'s loaded storage as well.
     */
    private static void mirrorAdd(MinecraftServer server, ResourceLocation typeId, UUID entity, UUID other) {
        TeamStorage storage = storageOf(server, entity);
        if (storage != null) storage.addRelated(typeId, other);
        TeamStorage otherStorage = storageOf(server, other);
        if (otherStorage != null) otherStorage.addInbound(typeId, entity);

        ServerPlayer entityPlayer = server.getPlayerList().getPlayer(entity);
        if (entityPlayer != null) sendNames(server, entityPlayer, List.of(other));
        ServerPlayer otherPlayer = server.getPlayerList().getPlayer(other);
        if (otherPlayer != null) sendNames(server, otherPlayer, List.of(entity));
    }

    /** Sends last-known names for {@code ids} to {@code target}, if any resolve. */
    private static void sendNames(MinecraftServer server, ServerPlayer target, Collection<UUID> ids) {
        Map<UUID, String> names = namesFor(server, ids);
        if (!names.isEmpty()) NetworkManager.sendToPlayer(target, new SyncNamesPayload(names));
    }

    /**
     * Removes {@code other} from {@code entity}'s loaded storage, if any, without touching the
     * rest of its set; mirrors the inbound side off {@code other}'s loaded storage as well.
     */
    private static void mirrorRemove(MinecraftServer server, ResourceLocation typeId, UUID entity, UUID other) {
        TeamStorage storage = storageOf(server, entity);
        if (storage != null) storage.removeRelated(typeId, other);
        TeamStorage otherStorage = storageOf(server, other);
        if (otherStorage != null) otherStorage.removeInbound(typeId, entity);
    }

    public static TeamResult tryAddRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (!isServer(a, "addRelation")) return TeamResult.CLIENT_SIDE;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        return tryAddRelation(serverOf(ra), type, ra.getUUID(), rb.getUUID());
    }

    public static boolean addRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        return tryAddRelation(type, a, b).isAccepted();
    }

    public static TeamResult tryRemoveRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (!isServer(a, "removeRelation")) return TeamResult.CLIENT_SIDE;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        return tryRemoveRelation(serverOf(ra), type, ra.getUUID(), rb.getUUID());
    }

    public static boolean removeRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        return tryRemoveRelation(type, a, b).isAccepted();
    }

    public static boolean hasRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        if (RelationResolver.teamsOf(ra).getRelated(type).contains(rb.getUUID())) return true;
        if (RelationResolver.teamsOf(rb).getRelatedBy(type).contains(ra.getUUID())) return true;
        if (!type.isSymmetricRelation()) return false;
        if (RelationResolver.teamsOf(rb).getRelated(type).contains(ra.getUUID())) return true;
        return RelationResolver.teamsOf(ra).getRelatedBy(type).contains(rb.getUUID());
    }

    /** Removes a dead non-player entity from every group and from every relation it is part of. */
    public static void removeEverywhere(MinecraftServer server, UUID entityId) {
        TeamSavedData data = TeamSavedData.get(server);
        for (UUID teamId : new ArrayList<>(data.getTeamIdsOf(entityId))) {
            data.getTeam(teamId).ifPresent(team -> removeMember(server, team, entityId, null, LeaveReason.DEATH));
        }

        Set<UUID> affected = new LinkedHashSet<>();
        Set<UUID> affectedTeams = new LinkedHashSet<>();
        Iterator<TeamInvite> it = data.getInvites().iterator();
        while (it.hasNext()) {
            TeamInvite invite = it.next();
            if (!invite.invitee().equals(entityId) && !invite.inviter().equals(entityId)) continue;
            it.remove();
            affected.add(invite.invitee());
            affectedTeams.add(invite.teamId());
        }

        affected.remove(entityId);
        for (UUID uuid : affected) syncInvites(server, uuid);
        for (UUID teamId : affectedTeams) data.getTeam(teamId).ifPresent(team -> syncTeamInvites(server, team));

        for (ResourceLocation typeId : data.getAllRelatedBy(entityId).keySet()) {
            for (UUID a : new ArrayList<>(data.getRelatedBy(typeId, entityId))) mirrorRemove(server, typeId, a, entityId);
        }
        for (ResourceLocation typeId : data.getAllRelated(entityId).keySet()) {
            for (UUID b : new ArrayList<>(data.getRelated(typeId, entityId))) mirrorRemove(server, typeId, entityId, b);
        }
        data.removeRelatedEverywhere(entityId);
    }

    public static void init() {
        EntityEvents.LIVING_PRE_DAMAGED.register((victim, source, amount) -> {
            if (victim.level().isClientSide()) return EventResult.pass();
            LivingEntity attacker = resolveAttacker(source);
            if (attacker == null || attacker == victim) return EventResult.pass();
            ResolvedRelation relation = RelationResolver.resolve(attacker, victim);
            if (relation.isAlly() && relation.decidedBy() != null && relation.decidedBy().blocksFriendlyFire()) return EventResult.interruptFalse();
            return EventResult.pass();
        });

        EntityEvents.LIVING_CHANGE_TARGET_EARLY.register((entity, changeableTarget) -> {
            if (entity.level().isClientSide()) return EventResult.pass();
            LivingEntity target = changeableTarget.get();
            if (target == null || target == entity) return EventResult.pass();
            ResolvedRelation relation = RelationResolver.resolve(entity, target);
            if (relation.isAlly() && relation.decidedBy() != null && relation.decidedBy().blocksTargeting()) {
                changeableTarget.set(null);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity.level().isClientSide() || entity instanceof Player) return EventResult.pass();
            MinecraftServer server = entity.getServer();
            if (server != null) removeEverywhere(server, entity.getUUID());
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            MinecraftServer server = player.getServer();
            if (server != null) {
                TeamSavedData data = TeamSavedData.get(server);
                data.putName(player.getUUID(), player.getGameProfile().getName());
                for (UUID teamId : data.getTeamIdsOf(player.getUUID())) {
                    data.getTeam(teamId).ifPresent(team -> {
                        if (team.isOwner(player) && !player.getGameProfile().getName().equals(team.getOwnerName())) {
                            Team.Internals.setOwnerName(team, player.getGameProfile().getName());
                            data.setDirty();
                            syncTeam(server, team);
                        }
                    });
                }
            }
            reconcile(player);
            syncAllFor(player);
            if (server != null) syncInvites(server, player.getUUID());
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> syncAllFor(player));
        PlayerEvent.CHANGE_DIMENSION.register((player, from, to) -> syncAllFor(player));

        EntityEvent.ADD.register((entity, level) -> {
            if (level.isClientSide() || !(entity instanceof LivingEntity living) || entity instanceof Player) return EventResult.pass();
            MinecraftServer server = level.getServer();
            if (server == null) return EventResult.pass();
            TeamStorage storage = storageOf(living);
            if (storage != null) reconcileRelations(server, living.getUUID(), storage);
            return EventResult.pass();
        });

        TickEvent.SERVER_POST.register(server -> {
            if (server.getTickCount() % 20 == 0) expireInvites(server);
            if (server.getTickCount() % 6000 == 0) {
                TeamSavedData data = TeamSavedData.get(server);
                Set<UUID> keep = data.referencedIds();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) keep.add(player.getUUID());
                data.pruneNames(keep);
            }
        });
    }

    @Nullable
    public static LivingEntity resolveAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity living) return living;
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) return owner;
        if (direct instanceof OwnableEntity ownable && ownable.getOwner() instanceof LivingEntity owner) return owner;
        return direct instanceof LivingEntity living ? living : null;
    }
}
