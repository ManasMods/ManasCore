/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.GameInstance;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.template.LeaveReason;
import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamAction;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.api.template.TeamResult;
import io.github.manasmods.manascore.team.api.template.Teams;
import io.github.manasmods.manascore.team.impl.OwnerResolvers;
import io.github.manasmods.manascore.team.impl.RelationResolver;
import io.github.manasmods.manascore.team.impl.SavedDataTeams;
import io.github.manasmods.manascore.team.impl.TeamManager;
import io.github.manasmods.manascore.team.impl.TeamRegistry;
import io.github.manasmods.manascore.team.impl.TeamSavedData;
import io.github.manasmods.manascore.team.impl.TeamStorage;
import io.github.manasmods.manascore.team.impl.builtin.AllyTeamType;
import io.github.manasmods.manascore.team.impl.builtin.PartyTeamType;
import io.github.manasmods.manascore.team.impl.client.ClientTeamCache;
import io.github.manasmods.manascore.team.impl.network.c2s.TeamActionPayload;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public entry point of the team module. Reads work on both sides; mutations are server-only
 * and return false / empty when called on the client.
 */
public class TeamAPI {
    private static final Set<EntityType<?>> MISSING_STORAGE_WARNED = ConcurrentHashMap.newKeySet();

    private TeamAPI() {
    }

    @SuppressWarnings("unchecked")
    private static TeamType<Team> typeOf(Team team) {
        return (TeamType<Team>) team.getType();
    }

    /** Built-in symmetric ally relation. Concrete class exposed so mods can reference it directly. */
    public static final RegistrySupplier<AllyTeamType> ALLY = TeamRegistry.ALLY;
    /** Built-in party team type. Concrete class exposed so mods can reference it directly. */
    public static final RegistrySupplier<PartyTeamType> PARTY = TeamRegistry.PARTY;

    public static Registrar<TeamType<?>> getTeamTypeRegistry() {
        return TeamRegistry.TEAM_TYPES;
    }

    public static ResourceKey<Registry<TeamType<?>>> getTeamTypeRegistryKey() {
        return TeamRegistry.KEY;
    }

    public static Teams getTeamsFrom(@NonNull LivingEntity entity) {
        Teams storage = entity.manasCore$getStorage(TeamStorage.getKey());
        if (storage != null) return storage;
        if (MISSING_STORAGE_WARNED.add(entity.getType())) {
            ManasCoreTeam.LOG.warn("Team storage is missing on entity type {} - falling back to a no-op storage.", EntityType.getKey(entity.getType()));
        }
        return Teams.EMPTY;
    }

    /** Server only. Team data of any id, loaded or not, read from the saved table. */
    public static Teams getTeamsFrom(@NonNull MinecraftServer server, @NonNull UUID id) {
        return new SavedDataTeams(TeamSavedData.get(server), id);
    }

    /**
     * Register an {@link OwnerResolver}. Resolvers are asked in registration order, the vanilla
     * {@link net.minecraft.world.entity.OwnableEntity} fallback last; the first non-null answer wins.
     */
    public static void registerOwnerResolver(@NonNull OwnerResolver resolver) {
        OwnerResolvers.register(resolver);
    }

    /**
     * Root owner id of the entity through every registered resolver, or its own id when it has
     * no owner. Works on both sides. See {@link OwnerResolver} for the chain rules.
     */
    public static UUID resolveOwner(@NonNull LivingEntity entity) {
        return OwnerResolvers.resolveId(entity);
    }

    public static ResolvedRelation resolveRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return RelationResolver.resolve(a, b);
    }

    public static Relation getRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return resolveRelation(a, b).relation();
    }

    public static boolean isAllied(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return getRelation(a, b) == Relation.ALLY;
    }

    /**
     * Determine if both sides consider each other an ally.
     * One-way relations such as the default ally type only count when they exist in both directions.
     */
    public static boolean isMutuallyAllied(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return isAllied(a, b) && isAllied(b, a);
    }

    public static boolean isEnemy(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return getRelation(a, b) == Relation.ENEMY;
    }

    /** Server: the saved table. Client: the local cache (own teams only). */
    public static Optional<Team> getTeam(@NonNull LivingEntity context, @NonNull UUID id) {
        MinecraftServer server = context.getServer();
        if (server != null && !context.level().isClientSide()) return TeamSavedData.get(server).getTeam(id);
        return ClientTeamCache.getTeam(id);
    }

    public static Optional<Team> getTeam(@NonNull MinecraftServer server, @NonNull UUID id) {
        return TeamSavedData.get(server).getTeam(id);
    }

    /** Server level: the saved table. Client level: the local cache (own teams only). */
    public static Optional<Team> getTeam(@NonNull Level level, @NonNull UUID id) {
        MinecraftServer server = level.getServer();
        if (server != null && !level.isClientSide()) return TeamSavedData.get(server).getTeam(id);
        return ClientTeamCache.getTeam(id);
    }

    public static <T extends Team> Optional<T> getTeam(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        Set<T> teams = getTeams(entity, type);
        return teams.isEmpty() ? Optional.empty() : Optional.of(teams.iterator().next());
    }

    public static <T extends Team> Set<T> getTeams(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        LivingEntity resolved = type.resolveMember(entity);
        Set<T> result = new LinkedHashSet<>();
        for (UUID id : getTeamsFrom(resolved).getTeamIds(type)) {
            getTeam(resolved, id).filter(type.getTeamClass()::isInstance).map(type.getTeamClass()::cast).ifPresent(result::add);
        }
        return Collections.unmodifiableSet(result);
    }

    public static List<TeamInvite> getPendingInvites(@NonNull LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server != null && !entity.level().isClientSide()) return TeamManager.getPendingInvites(server, entity.getUUID());
        return ClientTeamCache.getInvites();
    }

    /** Server: online profile name, last-known saved name, or the profile cache. Client: the local cache. */
    public static Optional<MemberInfo> getMemberInfo(@NonNull Level level, @NonNull UUID id) {
        if (level.isClientSide()) return ClientTeamCache.getMemberInfo(id);
        MinecraftServer server = level.getServer();
        return server == null ? Optional.empty() : TeamManager.memberInfo(server, id);
    }

    /** Server only. Safe from any thread. Online profile name, last-known saved name, or the profile cache. */
    public static Optional<MemberInfo> getMemberInfo(@NonNull MinecraftServer server, @NonNull UUID id) {
        return TeamManager.memberInfo(server, id);
    }

    /**
     * Client / GUI use only, for code without a {@link Level} or {@link MinecraftServer} in hand.
     * The side is guessed from the environment and the current thread: a dedicated server always
     * counts as server side, on a client only the integrated server thread does. Server code on a
     * worker thread of an integrated server would be misread as the client, so server code must use
     * the {@link MinecraftServer} or {@link Level} overloads.
     */
    public static Optional<MemberInfo> getMemberInfo(@NonNull UUID id) {
        MinecraftServer server = GameInstance.getServer();
        boolean serverSide = Platform.getEnvironment() == Env.SERVER || (server != null && server.isSameThread());
        return serverSide ? TeamManager.memberInfo(server, id) : ClientTeamCache.getMemberInfo(id);
    }

    /** Falls back to the first 8 characters of the uuid when no name is known. */
    public static Component getMemberName(@NonNull Level level, @NonNull UUID id) {
        return getMemberInfo(level, id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /** Server only. Safe from any thread. Falls back to the first 8 characters of the uuid when no name is known. */
    public static Component getMemberName(@NonNull MinecraftServer server, @NonNull UUID id) {
        return getMemberInfo(server, id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /**
     * Client / GUI use only, see {@link #getMemberInfo(UUID)} for the side rule.
     * Falls back to the first 8 characters of the uuid when no name is known.
     */
    public static Component getMemberName(@NonNull UUID id) {
        return getMemberInfo(id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /** Server only. Every team of every type. */
    public static Collection<Team> getAllTeams(@NonNull MinecraftServer server) {
        return TeamSavedData.get(server).getTeams();
    }

    /*** GROUP ops (server only) ***/
    public static <T extends Team> Optional<T> createTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner) {
        return TeamManager.createTeam(type, owner);
    }

    /** Like {@link #createTeam}, with the reason. */
    public static <T extends Team> TeamResult tryCreateTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner) {
        return TeamManager.tryCreateTeam(type, owner);
    }

    public static boolean disbandTeam(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.disbandTeam(server, team);
    }

    /** Like {@link #disbandTeam}, with the reason. */
    public static TeamResult tryDisbandTeam(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.tryDisbandTeam(server, team);
    }

    /** Bypasses invites. Respects limits, {@link TeamType#canJoin} and {@link TeamEvents#MEMBER_JOIN}. */
    public static boolean addMember(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.addMember(team, entity);
    }

    /**
     * Like {@link #addMember}, with the reason. Under LEAVE_OLDEST the evictions are applied
     * before the final team check, so NOT_FOUND can be returned after the entity already left
     * older teams.
     */
    public static TeamResult tryAddMember(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.tryAddMember(team, entity);
    }

    public static boolean removeMember(@NonNull Team team, @NonNull LivingEntity entity, @NonNull LeaveReason reason) {
        if (!TeamManager.isServer(entity, "removeMember")) return false;
        LivingEntity resolved = team.getType().resolveMember(entity);
        return TeamManager.removeMember(entity.getServer(), team, resolved.getUUID(), resolved, reason);
    }

    /** Like {@link #removeMember(Team, LivingEntity, LeaveReason)}, with the reason. */
    public static TeamResult tryRemoveMember(@NonNull Team team, @NonNull LivingEntity entity, @NonNull LeaveReason reason) {
        if (!TeamManager.isServer(entity, "removeMember")) return TeamResult.CLIENT_SIDE;
        LivingEntity resolved = team.getType().resolveMember(entity);
        return TeamManager.tryRemoveMember(entity.getServer(), team, resolved.getUUID(), resolved, reason);
    }

    /** Removes an offline or unloaded member by id. */
    public static boolean removeMember(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID memberId, @NonNull LeaveReason reason) {
        return TeamManager.removeMember(server, team, memberId, null, reason);
    }

    /** Like {@link #removeMember(MinecraftServer, Team, UUID, LeaveReason)}, with the reason. */
    public static TeamResult tryRemoveMember(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID memberId, @NonNull LeaveReason reason) {
        return TeamManager.tryRemoveMember(server, team, memberId, null, reason);
    }

    public static boolean setOwner(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.setOwner(team, entity);
    }

    /** Like {@link #setOwner}, with the reason. */
    public static TeamResult trySetOwner(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.trySetOwner(team, entity);
    }

    /** Determine if the inviter may send invites, subject to {@link TeamType#canInvite(Team, LivingEntity)}. */
    public static boolean canInvite(@NonNull Team team, @NonNull LivingEntity inviter) {
        TeamType<Team> type = typeOf(team);
        return type.canInvite(team, type.resolveMember(inviter));
    }

    /** Determine if the actor may kick the target member, subject to {@link TeamType#canKick}. */
    public static boolean canKick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        TeamType<Team> type = typeOf(team);
        return type.canKick(team, type.resolveMember(actor), target);
    }

    /** Determine if the actor may promote the target member, subject to {@link TeamType#canPromote}. */
    public static boolean canPromote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        TeamType<Team> type = typeOf(team);
        return type.canPromote(team, type.resolveMember(actor), target);
    }

    /** Determine if the actor may leave the team, subject to {@link TeamType#canLeave}. */
    public static boolean canLeave(@NonNull Team team, @NonNull LivingEntity actor) {
        TeamType<Team> type = typeOf(team);
        return type.canLeave(team, type.resolveMember(actor));
    }

    /** Determine if the actor may disband the team, subject to {@link TeamType#canDisband}. */
    public static boolean canDisband(@NonNull Team team, @NonNull LivingEntity actor) {
        TeamType<Team> type = typeOf(team);
        return type.canDisband(team, type.resolveMember(actor));
    }

    /** Kicks the target member, subject to {@link TeamType#canKick}. */
    public static TeamResult tryKick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.tryKick(team, actor, target);
    }

    /** Like {@link #tryKick}, collapsed to a boolean. */
    public static boolean kick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.kick(team, actor, target);
    }

    /** Promotes the target member to owner, subject to {@link TeamType#canPromote}. */
    public static TeamResult tryPromote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.tryPromote(team, actor, target);
    }

    /** Like {@link #tryPromote}, collapsed to a boolean. */
    public static boolean promote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.promote(team, actor, target);
    }

    /** Removes the actor from the team, subject to {@link TeamType#canLeave}. */
    public static TeamResult tryLeave(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.tryLeave(team, actor);
    }

    /** Like {@link #tryLeave}, collapsed to a boolean. */
    public static boolean leave(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.leave(team, actor);
    }

    /** Disbands the team, subject to {@link TeamType#canDisband}. */
    public static TeamResult tryDisband(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.tryDisband(team, actor);
    }

    /** Like {@link #tryDisband}, collapsed to a boolean. */
    public static boolean disband(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.disband(team, actor);
    }

    /** Sanitizes and applies {@code name}, subject to {@link TeamType#canRename} and {@link TeamType#getMaxNameLength()}. */
    public static boolean setTeamName(@NonNull Team team, @NonNull LivingEntity actor, @Nullable String name) {
        return TeamManager.setTeamName(team, actor, name);
    }

    /** Like {@link #setTeamName(Team, LivingEntity, String)}, with the reason. */
    public static TeamResult trySetTeamName(@NonNull Team team, @NonNull LivingEntity actor, @Nullable String name) {
        return TeamManager.trySetTeamName(team, actor, name);
    }

    /** Server-authoritative variant, bypassing {@link TeamType#canRename}. */
    public static boolean setTeamName(@NonNull MinecraftServer server, @NonNull Team team, @Nullable String name) {
        return TeamManager.setTeamName(server, team, name);
    }

    /** Like {@link #setTeamName(MinecraftServer, Team, String)}, with the reason. */
    public static TeamResult trySetTeamName(@NonNull MinecraftServer server, @NonNull Team team, @Nullable String name) {
        return TeamManager.trySetTeamName(server, team, name);
    }

    public static Optional<TeamInvite> invite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull LivingEntity invitee) {
        return TeamManager.invite(team, inviter, invitee);
    }

    /** Like {@link #invite}, with the reason. */
    public static TeamResult tryInvite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull LivingEntity invitee) {
        return TeamManager.tryInvite(team, inviter, invitee);
    }

    public static boolean acceptInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.acceptInvite(invitee, teamId);
    }

    /**
     * Like {@link #acceptInvite}, with the reason. An invite of an unregistered type is removed
     * and synced before NOT_FOUND is returned; the join result (for example TEAM_FULL) is
     * propagated as-is.
     */
    public static TeamResult tryAcceptInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.tryAcceptInvite(invitee, teamId);
    }

    public static boolean declineInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.declineInvite(invitee, teamId);
    }

    /** Like {@link #declineInvite}, with the reason. */
    public static TeamResult tryDeclineInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.tryDeclineInvite(invitee, teamId);
    }

    /**
     * Server only. Online players this team may still invite, with every limit and hook applied.
     * Players with a pending invite are excluded; see {@link #getOutgoingInvites}.
     */
    public static List<ServerPlayer> getInvitable(@NonNull Team team, @NonNull ServerPlayer requester) {
        return TeamManager.getInvitable(team, requester);
    }

    /**
     * Client: last list received for {@link Client#requestInvitable}. Server: computed now.
     */
    public static List<UUID> getInvitable(@NonNull LivingEntity viewer, @NonNull UUID teamId) {
        if (viewer.level().isClientSide()) return ClientTeamCache.getInvitable(teamId);
        MinecraftServer server = viewer.getServer();
        if (server == null || !(viewer instanceof ServerPlayer player)) return List.of();
        Optional<Team> team = getTeam(server, teamId);
        if (team.isEmpty()) return List.of();
        return getInvitable(team.get(), player).stream().map(ServerPlayer::getUUID).toList();
    }

    /** Server only. This team's pending outgoing invites. */
    public static List<TeamInvite> getOutgoingInvites(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.getOutgoingInvites(server, team);
    }

    /** Server: the saved table. Client: the local cache (own teams only). */
    public static List<TeamInvite> getOutgoingInvites(@NonNull LivingEntity viewer, @NonNull Team team) {
        if (!viewer.level().isClientSide()) return TeamManager.getOutgoingInvites(viewer.getServer(), team);
        return ClientTeamCache.getOutgoingInvites(team.getId());
    }

    /** Server only. Whether the given player already has a pending invite into this team. */
    public static boolean hasPendingInvite(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID invitee) {
        return TeamManager.hasPendingInvite(server, team, invitee);
    }

    /*** RELATION ops (server only) ***/
    public static boolean addRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.addRelation(type, a, b);
    }

    /** Like {@link #addRelation(TeamType, LivingEntity, LivingEntity)}, with the reason. */
    public static TeamResult tryAddRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.tryAddRelation(type, a, b);
    }

    public static boolean removeRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.removeRelation(type, a, b);
    }

    /** Like {@link #removeRelation(TeamType, LivingEntity, LivingEntity)}, with the reason. */
    public static TeamResult tryRemoveRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.tryRemoveRelation(type, a, b);
    }

    public static boolean hasRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.hasRelation(type, a, b);
    }

    /** Entities that list this one under the given RELATION type. Works on both sides for tracked entities. */
    public static Set<UUID> getRelatedBy(@NonNull LivingEntity entity, @NonNull TeamType<?> type) {
        return getTeamsFrom(type.resolveMember(entity)).getRelatedBy(type);
    }

    /**
     * Determine if the relation exists in both directions.
     */
    public static boolean hasMutualRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.hasRelation(type, a, b) && TeamManager.hasRelation(type, b, a);
    }

    /**
     * Id-based RELATION op. Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated
     * on their next login; until then their old relations are not visible to this method.
     */
    public static boolean addRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.addRelation(server, type, a, b);
    }

    /** Like {@link #addRelation(MinecraftServer, TeamType, UUID, UUID)}, with the reason. */
    public static TeamResult tryAddRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.tryAddRelation(server, type, a, b);
    }

    /**
     * Id-based RELATION op. Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated
     * on their next login; until then their old relations are not visible to this method.
     */
    public static boolean removeRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.removeRelation(server, type, a, b);
    }

    /** Like {@link #removeRelation(MinecraftServer, TeamType, UUID, UUID)}, with the reason. */
    public static TeamResult tryRemoveRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.tryRemoveRelation(server, type, a, b);
    }

    /**
     * Id-based RELATION op. Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated
     * on their next login; until then their old relations are not visible to this method.
     */
    public static boolean hasRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.hasRelation(server, type, a, b);
    }

    public static Set<UUID> getRelated(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID entity) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(TeamManager.getRelated(server, type, entity)));
    }

    /** Server-side, id-based counterpart of {@link #getRelatedBy(LivingEntity, TeamType)}. */
    public static Set<UUID> getRelatedBy(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID entity) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(TeamManager.getRelatedBy(server, type, entity)));
    }

    /** Client-side senders for {@link TeamActionPayload}. Every mutation goes through the server's {@code try*} handlers. */
    public static final class Client {
        private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

        private Client() {
        }

        private static boolean guard(String op) {
            MinecraftServer server = GameInstance.getServer();
            if (Platform.getEnvironment() == Env.SERVER || (server != null && server.isSameThread())) {
                if (WARNED.add(op)) ManasCoreTeam.LOG.warn("TeamAPI.Client.{} called on the server thread.", op);
                return true;
            }
            return false;
        }

        @Nullable
        private static String clamp(@Nullable String name) {
            if (name != null && name.length() > 256) return name.substring(0, 256);
            return name;
        }

        public static void create(@NonNull TeamType<?> type, @Nullable String name) {
            if (guard("create")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.CREATE, type.getId(), null, null, clamp(name)));
        }

        public static void rename(@NonNull UUID teamId, @Nullable String name) {
            if (guard("rename")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.RENAME, null, teamId, null, clamp(name)));
        }

        public static void invite(@NonNull UUID teamId, @NonNull UUID invitee) {
            if (guard("invite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.INVITE, null, teamId, invitee, null));
        }

        public static void kick(@NonNull UUID teamId, @NonNull UUID member) {
            if (guard("kick")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.KICK, null, teamId, member, null));
        }

        public static void promote(@NonNull UUID teamId, @NonNull UUID member) {
            if (guard("promote")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.PROMOTE, null, teamId, member, null));
        }

        public static void leave(@NonNull UUID teamId) {
            if (guard("leave")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.LEAVE, null, teamId, null, null));
        }

        public static void disband(@NonNull UUID teamId) {
            if (guard("disband")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.DISBAND, null, teamId, null, null));
        }

        public static void acceptInvite(@NonNull UUID teamId) {
            if (guard("acceptInvite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.ACCEPT_INVITE, null, teamId, null, null));
        }

        public static void declineInvite(@NonNull UUID teamId) {
            if (guard("declineInvite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.DECLINE_INVITE, null, teamId, null, null));
        }

        public static void addRelation(@NonNull TeamType<?> type, @NonNull UUID target) {
            if (guard("addRelation")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.ADD_RELATION, type.getId(), null, target, null));
        }

        public static void removeRelation(@NonNull TeamType<?> type, @NonNull UUID target) {
            if (guard("removeRelation")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.REMOVE_RELATION, type.getId(), null, target, null));
        }

        public static void requestInvitable(@NonNull UUID teamId) {
            if (guard("requestInvitable")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.REQUEST_INVITABLE, null, teamId, null, null));
        }
    }
}
