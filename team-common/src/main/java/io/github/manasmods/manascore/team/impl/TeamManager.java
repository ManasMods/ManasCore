/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.LeaveReason;
import io.github.manasmods.manascore.team.api.template.LimitPolicy;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.api.template.TeamShape;
import io.github.manasmods.manascore.team.impl.network.s2c.RemoveTeamPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncInvitesPayload;
import io.github.manasmods.manascore.team.impl.network.s2c.SyncTeamPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    public static <T extends Team> Optional<T> createTeam(TeamType<T> type, LivingEntity owner) {
        if (!isServer(owner, "createTeam")) return Optional.empty();
        if (type.getShape() != TeamShape.GROUP) return Optional.empty();
        LivingEntity resolved = type.resolveMember(owner);
        TeamStorage storage = storageOf(resolved);
        if (storage == null) return Optional.empty();

        MinecraftServer server = serverOf(resolved);
        List<UUID> evict = planEvictions(server, type, resolved, storage);
        if (evict == null) return Optional.empty();
        if (TeamEvents.TEAM_CREATE.invoker().create(type, resolved).isFalse()) return Optional.empty();
        applyEvictions(server, type, resolved, storage, evict);

        T team = type.createTeam(UUID.randomUUID(), resolved.getUUID());
        TeamSavedData.get(server).putTeam(team);
        storage.addTeamId(idOf(type), team.getId());
        type.onTeamCreated(team);
        TeamEvents.TEAM_CREATED.invoker().run(team);
        syncTeam(server, team);
        return Optional.of(team);
    }

    public static boolean disbandTeam(MinecraftServer server, Team team) {
        if (TeamEvents.TEAM_DISBAND.invoker().run(team).isFalse()) return false;
        disbandInternal(server, team);
        return true;
    }

    private static void disbandInternal(MinecraftServer server, Team team) {
        TeamSavedData data = TeamSavedData.get(server);
        ResourceLocation typeId = idOf(team.getType());
        for (UUID member : new ArrayList<>(team.getMembers())) {
            Team.Internals.removeMember(team, member);
            TeamStorage storage = storageOf(server, member);

            if (storage != null) storage.removeTeamId(typeId, team.getId());
            sendRemove(server, team, member);
            typeOf(team).onMemberRemoved(cast(team), member, LeaveReason.DISBAND);
            TeamEvents.MEMBER_LEFT.invoker().run(team, member, LeaveReason.DISBAND);
        }

        data.getInvites().removeIf(invite -> invite.teamId().equals(team.getId()));
        data.removeTeam(team.getId());
        typeOf(team).onTeamDisbanded(cast(team));
        TeamEvents.TEAM_DISBANDED.invoker().run(team);
    }

    public static boolean addMember(Team team, LivingEntity entity) {
        if (!isServer(entity, "addMember")) return false;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolved = type.resolveMember(entity);
        TeamStorage storage = storageOf(resolved);
        if (storage == null) return false;
        if (team.isMember(resolved)) return false;
        if (team.size() >= type.getMaxMembers()) return false;
        if (!type.canJoin(team, resolved)) return false;

        MinecraftServer server = serverOf(resolved);
        List<UUID> evict = planEvictions(server, type, resolved, storage);
        if (evict == null) return false;
        if (TeamEvents.MEMBER_JOIN.invoker().run(team, resolved).isFalse()) return false;
        applyEvictions(server, type, resolved, storage, evict);
        if (TeamSavedData.get(server).getTeam(team.getId()).isEmpty()) return false;

        Team.Internals.addMember(team, resolved.getUUID());
        storage.addTeamId(idOf(type), team.getId());
        TeamSavedData.get(server).setDirty();
        type.onMemberAdded(team, resolved.getUUID());
        TeamEvents.MEMBER_JOINED.invoker().run(team, resolved);
        syncTeam(server, team);
        return true;
    }

    /**
     * Removes a member. {@code entity} is the loaded entity when available; null for offline
     * or dead members. {@link TeamEvents#MEMBER_LEAVE} fires only for LEAVE and KICK.
     */
    public static boolean removeMember(MinecraftServer server, Team team, UUID memberId, @Nullable LivingEntity entity, LeaveReason reason) {
        if (!team.isMember(memberId)) return false;
        boolean cancellable = reason == LeaveReason.LEAVE || reason == LeaveReason.KICK;
        if (cancellable && entity != null && TeamEvents.MEMBER_LEAVE.invoker().run(team, entity, reason).isFalse()) return false;

        TeamType<Team> type = typeOf(team);
        boolean wasOwner = team.isOwner(memberId);
        Team.Internals.removeMember(team, memberId);
        TeamStorage storage = entity != null ? storageOf(entity) : storageOf(server, memberId);
        if (storage != null) storage.removeTeamId(idOf(type), team.getId());
        sendRemove(server, team, memberId);
        type.onMemberRemoved(team, memberId, reason);
        TeamEvents.MEMBER_LEFT.invoker().run(team, memberId, reason);

        if (team.size() == 0) {
            disbandInternal(server, team);
            return true;
        }

        if (wasOwner) {
            UUID newOwner = type.pickNewOwner(team);
            if (newOwner == null || !team.isMember(newOwner)) {
                disbandInternal(server, team);
                return true;
            }
            Team.Internals.setOwner(team, newOwner);
            TeamEvents.OWNER_CHANGED.invoker().run(team, memberId, newOwner);
        }

        TeamSavedData.get(server).setDirty();
        syncTeam(server, team);
        return true;
    }

    public static boolean setOwner(Team team, LivingEntity entity) {
        if (!isServer(entity, "setOwner")) return false;
        LivingEntity resolved = typeOf(team).resolveMember(entity);
        if (!team.isMember(resolved)) return false;
        if (team.isOwner(resolved)) return false;

        UUID old = team.getOwner();
        Team.Internals.setOwner(team, resolved.getUUID());
        MinecraftServer server = serverOf(resolved);
        TeamSavedData.get(server).setDirty();
        TeamEvents.OWNER_CHANGED.invoker().run(team, old, resolved.getUUID());
        syncTeam(server, team);
        return true;
    }

    public static boolean setTeamName(MinecraftServer server, Team team, @Nullable String name) {
        String sanitized = sanitizeName(name);
        String old = team.getName();
        if (Objects.equals(old, sanitized)) return false;
        if (sanitized != null && sanitized.length() > typeOf(team).getMaxNameLength()) return false;

        Team.Internals.setName(team, sanitized);
        TeamSavedData.get(server).setDirty();
        TeamEvents.TEAM_RENAMED.invoker().run(team, old, sanitized);
        syncTeam(server, team);
        return true;
    }

    public static boolean setTeamName(Team team, LivingEntity actor, @Nullable String name) {
        if (!isServer(actor, "setTeamName")) return false;
        TeamType<Team> type = typeOf(team);
        LivingEntity resolved = type.resolveMember(actor);
        if (!type.canRename(team, resolved)) return false;
        return setTeamName(serverOf(resolved), team, name);
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
        SyncTeamPayload payload = new SyncTeamPayload(idOf(team.getType()), TeamSavedData.serialize(team));
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
        for (Team team : TeamSavedData.get(server).getTeams()) {
            if (!team.isMember(player)) continue;
            SyncTeamPayload payload = new SyncTeamPayload(idOf(team.getType()), TeamSavedData.serialize(team));
            NetworkManager.sendToPlayer(player, payload);
        }
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

        Map<ResourceLocation, List<UUID>> byType = new HashMap<>();
        for (Team team : TeamSavedData.get(server).getTeams()) {
            if (!team.isMember(player)) continue;
            byType.computeIfAbsent(idOf(team.getType()), k -> new ArrayList<>()).add(team.getId());
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
    }

    public static Optional<TeamInvite> invite(Team team, LivingEntity inviter, LivingEntity invitee) {
        if (!isServer(inviter, "invite")) return Optional.empty();
        TeamType<Team> type = typeOf(team);
        LivingEntity resolvedInviter = type.resolveMember(inviter);
        LivingEntity resolvedInvitee = type.resolveMember(invitee);
        if (team.isMember(resolvedInvitee)) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", "already-member", team.getId(), resolvedInviter.getUUID(), resolvedInvitee.getUUID());
            return Optional.empty();
        }
        if (!type.canInvite(team, resolvedInviter, resolvedInvitee)) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", "not-allowed", team.getId(), resolvedInviter.getUUID(), resolvedInvitee.getUUID());
            return Optional.empty();
        }
        return createInvite(serverOf(inviter), team.getId(), idOf(type), resolvedInviter, resolvedInvitee, type.getInviteTimeoutTicks());
    }

    /** Server only. Online players this team may still invite, with every limit and hook applied. */
    public static List<ServerPlayer> getInvitable(Team team, ServerPlayer requester) {
        MinecraftServer server = requester.getServer();
        TeamType<Team> type = typeOf(team);
        if (team.size() >= type.getMaxMembers()) return List.of();

        LivingEntity resolvedRequester = type.resolveMember(requester);
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            LivingEntity candidate = type.resolveMember(p);
            if (candidate.getUUID().equals(resolvedRequester.getUUID())) continue;
            if (team.isMember(candidate)) continue;
            if (hasPendingInvite(server, team, candidate.getUUID())) continue;
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

    private static Optional<TeamInvite> createInvite(MinecraftServer server, UUID teamId, ResourceLocation typeId, LivingEntity inviter, LivingEntity invitee, int timeoutTicks) {
        TeamSavedData data = TeamSavedData.get(server);
        if (data.findInvite(invitee.getUUID(), teamId).isPresent()) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", "pending", teamId, inviter.getUUID(), invitee.getUUID());
            return Optional.empty();
        }
        TeamInvite invite = new TeamInvite(teamId, typeId, inviter.getUUID(), invitee.getUUID(), server.getTickCount() + timeoutTicks);
        if (TeamEvents.INVITE_SEND.invoker().run(invite).isFalse()) {
            ManasCoreTeam.LOG.debug("Invite refused ({}): team {} inviter {} invitee {}", "cancelled", teamId, inviter.getUUID(), invitee.getUUID());
            return Optional.empty();
        }

        data.getInvites().add(invite);
        syncInvites(server, invite.invitee());
        return Optional.of(invite);
    }

    /**
     * Returns false when the invite does not exist, the type is unknown or the join was rejected.
     */
    public static boolean acceptInvite(LivingEntity invitee, UUID teamId) {
        if (!isServer(invitee, "acceptInvite")) return false;
        MinecraftServer server = serverOf(invitee);
        TeamSavedData data = TeamSavedData.get(server);
        Optional<TeamInvite> optional = data.findInvite(invitee.getUUID(), teamId);
        if (optional.isEmpty()) return false;

        TeamInvite invite = optional.get();
        TeamType<?> type = TeamRegistry.TEAM_TYPES.get(invite.typeId());
        if (type == null) {
            data.getInvites().remove(invite);
            syncInvites(server, invitee.getUUID());
            return false;
        }

        Optional<Team> team = data.getTeam(invite.teamId());
        if (team.isEmpty() || !addMember(team.get(), invitee)) return false;

        data.getInvites().remove(invite);
        TeamEvents.INVITE_ACCEPTED.invoker().run(invite);
        syncInvites(server, invitee.getUUID());
        return true;
    }

    public static boolean declineInvite(LivingEntity invitee, UUID teamId) {
        if (!isServer(invitee, "declineInvite")) return false;
        MinecraftServer server = serverOf(invitee);
        TeamSavedData data = TeamSavedData.get(server);
        Optional<TeamInvite> optional = data.findInvite(invitee.getUUID(), teamId);

        if (optional.isEmpty()) return false;
        data.getInvites().remove(optional.get());
        TeamEvents.INVITE_DECLINED.invoker().run(optional.get());
        syncInvites(server, invitee.getUUID());
        return true;
    }

    public static List<TeamInvite> getPendingInvites(MinecraftServer server, UUID invitee) {
        return TeamSavedData.get(server).getInvitesFor(invitee);
    }

    public static void syncInvites(MinecraftServer server, UUID invitee) {
        ServerPlayer player = server.getPlayerList().getPlayer(invitee);
        if (player == null) return;
        List<TeamInvite> invites = getPendingInvites(server, invitee);
        NetworkManager.sendToPlayer(player, new SyncInvitesPayload(invites));
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
        for (TeamInvite invite : expired) {
            affected.add(invite.invitee());
            TeamEvents.INVITE_EXPIRED.invoker().run(invite);
        }
        for (UUID invitee : affected) syncInvites(server, invitee);
    }

    public static boolean addRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        if (a.equals(b)) return false;
        if (TeamEvents.RELATION_ADD.invoker().run(type, a, b).isFalse()) return false;

        ResourceLocation typeId = idOf(type);
        TeamSavedData data = TeamSavedData.get(server);
        boolean changed = data.addRelated(typeId, a, b);
        if (type.isSymmetricRelation()) changed |= data.addRelated(typeId, b, a);
        if (!changed) return false;

        mirrorAdd(server, typeId, a, b);
        if (type.isSymmetricRelation()) mirrorAdd(server, typeId, b, a);
        TeamEvents.RELATION_ADDED.invoker().run(type, a, b);
        return true;
    }

    public static boolean removeRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        if (a.equals(b)) return false;

        ResourceLocation typeId = idOf(type);
        TeamSavedData data = TeamSavedData.get(server);
        boolean changed = data.removeRelated(typeId, a, b);
        if (type.isSymmetricRelation()) changed |= data.removeRelated(typeId, b, a);
        if (!changed) return false;

        mirrorRemove(server, typeId, a, b);
        if (type.isSymmetricRelation()) mirrorRemove(server, typeId, b, a);
        TeamEvents.RELATION_REMOVED.invoker().run(type, a, b);
        return true;
    }

    public static boolean hasRelation(MinecraftServer server, TeamType<?> type, UUID a, UUID b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        if (a.equals(b)) return false;
        return TeamSavedData.get(server).getRelated(idOf(type), a).contains(b);
    }

    public static Set<UUID> getRelated(MinecraftServer server, TeamType<?> type, UUID entity) {
        return TeamSavedData.get(server).getRelated(idOf(type), entity);
    }

    /** Adds {@code other} to {@code entity}'s loaded storage, if any, without touching the rest of its set. */
    private static void mirrorAdd(MinecraftServer server, ResourceLocation typeId, UUID entity, UUID other) {
        TeamStorage storage = storageOf(server, entity);
        if (storage != null) storage.addRelated(typeId, other);
    }

    /** Removes {@code other} from {@code entity}'s loaded storage, if any, without touching the rest of its set. */
    private static void mirrorRemove(MinecraftServer server, ResourceLocation typeId, UUID entity, UUID other) {
        TeamStorage storage = storageOf(server, entity);
        if (storage != null) storage.removeRelated(typeId, other);
    }

    public static boolean addRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (!isServer(a, "addRelation")) return false;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        return addRelation(serverOf(ra), type, ra.getUUID(), rb.getUUID());
    }

    public static boolean removeRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (!isServer(a, "removeRelation")) return false;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        return removeRelation(serverOf(ra), type, ra.getUUID(), rb.getUUID());
    }

    public static boolean hasRelation(TeamType<?> type, LivingEntity a, LivingEntity b) {
        if (type.getShape() != TeamShape.RELATION) return false;
        LivingEntity ra = type.resolveMember(a);
        LivingEntity rb = type.resolveMember(b);
        if (RelationResolver.teamsOf(ra).getRelated(type).contains(rb.getUUID())) return true;
        return type.isSymmetricRelation() && RelationResolver.teamsOf(rb).getRelated(type).contains(ra.getUUID());
    }

    /** Removes a dead non-player entity from every group and from every online player's relation sets. */
    public static void removeEverywhere(MinecraftServer server, UUID entityId) {
        TeamSavedData data = TeamSavedData.get(server);
        for (Team team : new ArrayList<>(data.getTeams())) {
            if (team.isMember(entityId)) removeMember(server, team, entityId, null, LeaveReason.DEATH);
        }

        Set<UUID> affected = new LinkedHashSet<>();
        Iterator<TeamInvite> it = data.getInvites().iterator();
        while (it.hasNext()) {
            TeamInvite invite = it.next();
            if (!invite.invitee().equals(entityId) && !invite.inviter().equals(entityId)) continue;
            it.remove();
            affected.add(invite.invitee());
        }

        affected.remove(entityId);
        for (UUID uuid : affected) syncInvites(server, uuid);

        data.removeRelatedEverywhere(entityId);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            TeamStorage storage = storageOf(player);
            if (storage == null) continue;
            for (ResourceLocation typeId : storage.getAllRelated().keySet()) storage.removeRelated(typeId, entityId);
        }
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
