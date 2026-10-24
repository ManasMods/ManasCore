/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
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
import net.minecraft.resources.ResourceLocation;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the public entry point of the team module.
 * <p>
 * Reads work on both sides. Mutations are server only and return false, empty or {@link TeamResult#CLIENT_SIDE} when called on the client.
 * Client code sends its requests to the server through {@link Client}.
 * <p>
 * Methods starting with {@code try} return a {@link TeamResult} with the reason. Their plain counterparts collapse it to a boolean or an {@link Optional}.
 */
public class TeamAPI {
    private static final Set<EntityType<?>> MISSING_STORAGE_WARNED = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean MARK_DIRTY_WARNED = new AtomicBoolean();

    private TeamAPI() {
    }

    @SuppressWarnings("unchecked")
    private static TeamType<Team> typeOf(Team team) {
        return (TeamType<Team>) team.getType();
    }

    /**
     * Built-in one-way ally relation.
     * The concrete class is exposed so mods can reference it directly.
     */
    public static final RegistrySupplier<AllyTeamType> ALLY = TeamRegistry.ALLY;
    /**
     * Built-in party team type.
     * The concrete class is exposed so mods can reference it directly.
     */
    public static final RegistrySupplier<PartyTeamType> PARTY = TeamRegistry.PARTY;

    /**
     * This Method returns the {@link TeamType} Registry.
     * It can be used to load {@link TeamType}s from the Registry.
     */
    public static Registrar<TeamType<?>> getTeamTypeRegistry() {
        return TeamRegistry.TEAM_TYPES;
    }

    /**
     * This Method returns the Registry Key of the {@link TeamType} Registry.
     * It can be used to create {@link dev.architectury.registry.registries.DeferredRegister} instances.
     */
    public static ResourceKey<Registry<TeamType<?>>> getTeamTypeRegistryKey() {
        return TeamRegistry.KEY;
    }

    /**
     * Can be used to load the {@link Teams} from a {@link LivingEntity}.
     * It returns {@link Teams#EMPTY} if the entity has no team storage.
     */
    public static Teams getTeamsFrom(@NonNull LivingEntity entity) {
        Teams storage = entity.manasCore$getStorage(TeamStorage.getKey());
        if (storage != null) return storage;
        if (MISSING_STORAGE_WARNED.add(entity.getType())) {
            ManasCoreTeam.LOG.warn("Team storage is missing on entity type {} - falling back to a no-op storage.", EntityType.getKey(entity.getType()));
        }
        return Teams.EMPTY;
    }

    /**
     * Can be used to load the {@link Teams} of any id, loaded or not, from the saved table.
     * This is server only.
     */
    public static Teams getTeamsFrom(@NonNull MinecraftServer server, @NonNull UUID id) {
        return new SavedDataTeams(TeamSavedData.get(server), id);
    }

    /**
     * Used to register an {@link OwnerResolver}.
     * Resolvers are asked in registration order, the vanilla {@link net.minecraft.world.entity.OwnableEntity} fallback last, and the first non-null answer wins.
     *
     * @param resolver {@link OwnerResolver} to register.
     */
    public static void registerOwnerResolver(@NonNull OwnerResolver resolver) {
        OwnerResolvers.register(resolver);
    }

    /**
     * Used to get the root owner id of the entity through every registered resolver, or its own id when it has no owner.
     * This works on both sides, see {@link OwnerResolver} for the chain rules.
     */
    public static UUID resolveOwner(@NonNull LivingEntity entity) {
        return OwnerResolvers.resolveId(entity);
    }

    /**
     * Used to resolve the relation of {@code a} towards {@code b}, together with the {@link TeamType} that decided it.
     *
     * @see #getRelation
     */
    public static ResolvedRelation resolveRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return RelationResolver.resolve(a, b);
    }

    /**
     * Used to get the {@link Relation} of {@code a} towards {@code b}.
     *
     * @see #resolveRelation
     */
    public static Relation getRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return resolveRelation(a, b).relation();
    }

    /**
     * Determine if {@code a} considers {@code b} an ally.
     * The check is one way, see {@link #isMutuallyAllied} for both directions.
     */
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

    /**
     * Determine if {@code a} considers {@code b} an enemy.
     */
    public static boolean isEnemy(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return getRelation(a, b) == Relation.ENEMY;
    }

    /**
     * Used to get the team with the id.
     * On the server this reads the saved table, on the client the local cache, which only holds the teams of the local player.
     */
    public static Optional<Team> getTeam(@NonNull LivingEntity context, @NonNull UUID id) {
        MinecraftServer server = context.getServer();
        if (server != null && !context.level().isClientSide()) return TeamSavedData.get(server).getTeam(id);
        return ClientTeamCache.getTeam(id);
    }

    /**
     * Used to get the team with the id from the saved table.
     * This is server only.
     */
    public static Optional<Team> getTeam(@NonNull MinecraftServer server, @NonNull UUID id) {
        return TeamSavedData.get(server).getTeam(id);
    }

    /**
     * Used to get the team with the id.
     * On a server level this reads the saved table, on a client level the local cache, which only holds the teams of the local player.
     */
    public static Optional<Team> getTeam(@NonNull Level level, @NonNull UUID id) {
        MinecraftServer server = level.getServer();
        if (server != null && !level.isClientSide()) return TeamSavedData.get(server).getTeam(id);
        return ClientTeamCache.getTeam(id);
    }

    /**
     * Used to get the first team of the type that the entity belongs to.
     * The entity is resolved through the ownership rules of the type.
     */
    public static <T extends Team> Optional<T> getTeam(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        Set<T> teams = getTeams(entity, type);
        return teams.isEmpty() ? Optional.empty() : Optional.of(teams.iterator().next());
    }

    /**
     * Used to get every team of the type that the entity belongs to.
     * The entity is resolved through the ownership rules of the type.
     */
    public static <T extends Team> Set<T> getTeams(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        LivingEntity resolved = type.resolveMember(entity);
        Set<T> result = new LinkedHashSet<>();
        for (UUID id : getTeamsFrom(resolved).getTeamIds(type)) {
            getTeam(resolved, id).filter(type.getTeamClass()::isInstance).map(type.getTeamClass()::cast).ifPresent(result::add);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Used to get the pending invites sent to the entity.
     * On the server this reads the saved table, on the client the local cache.
     */
    public static List<TeamInvite> getPendingInvites(@NonNull LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server != null && !entity.level().isClientSide()) return TeamManager.getPendingInvites(server, entity.getUUID());
        return ClientTeamCache.getInvites();
    }

    /**
     * Used to get the {@link MemberInfo} of an id.
     * On the server this is the online profile name, the last known saved name or the profile cache, on the client the local cache.
     */
    public static Optional<MemberInfo> getMemberInfo(@NonNull Level level, @NonNull UUID id) {
        if (level.isClientSide()) return ClientTeamCache.getMemberInfo(id);
        MinecraftServer server = level.getServer();
        return server == null ? Optional.empty() : TeamManager.memberInfo(server, id);
    }

    /**
     * Used to get the {@link MemberInfo} of an id from the online profile name, the last known saved name or the profile cache.
     * This is server only and safe from any thread.
     */
    public static Optional<MemberInfo> getMemberInfo(@NonNull MinecraftServer server, @NonNull UUID id) {
        return TeamManager.memberInfo(server, id);
    }

    /**
     * Used to get the {@link MemberInfo} of an id for client and GUI code without a {@link Level} or {@link MinecraftServer} in hand.
     * The side is guessed from the environment and the current thread: a dedicated server always counts as server side, on a client only the integrated server thread does.
     * Server code on a worker thread of an integrated server would be misread as the client, so server code must use the {@link MinecraftServer} or {@link Level} overloads.
     */
    public static Optional<MemberInfo> getMemberInfo(@NonNull UUID id) {
        MinecraftServer server = GameInstance.getServer();
        boolean serverSide = Platform.getEnvironment() == Env.SERVER || (server != null && server.isSameThread());
        return serverSide ? TeamManager.memberInfo(server, id) : ClientTeamCache.getMemberInfo(id);
    }

    /**
     * Used to get the name of an id as a {@link Component}.
     * It falls back to the first 8 characters of the uuid when no name is known.
     */
    public static Component getMemberName(@NonNull Level level, @NonNull UUID id) {
        return getMemberInfo(level, id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /**
     * Used to get the name of an id as a {@link Component}.
     * It falls back to the first 8 characters of the uuid when no name is known.
     * This is server only and safe from any thread.
     */
    public static Component getMemberName(@NonNull MinecraftServer server, @NonNull UUID id) {
        return getMemberInfo(server, id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /**
     * Used to get the name of an id as a {@link Component} for client and GUI code, see {@link #getMemberInfo(UUID)} for the side rule.
     * It falls back to the first 8 characters of the uuid when no name is known.
     */
    public static Component getMemberName(@NonNull UUID id) {
        return getMemberInfo(id).map(MemberInfo::name).orElseGet(() -> Component.literal(id.toString().substring(0, 8)));
    }

    /**
     * Used to get every team of every type.
     * This is server only.
     */
    public static Collection<Team> getAllTeams(@NonNull MinecraftServer server) {
        return TeamSavedData.get(server).getTeams();
    }

    /**
     * Used to save the team and send the whole team tag to every online member again, so do not store secrets in it.
     *
     * @see Team#markDirty()
     */
    public static void markDirty(@NonNull MinecraftServer server, @NonNull Team team) {
        TeamSavedData.get(server).setDirty();
        TeamManager.syncTeam(server, team);
    }

    /**
     * Used to save the team and send it to every online member, resolving the server on its own.
     * This is ignored with a warning when called on the client.
     *
     * @see #markDirty(MinecraftServer, Team)
     */
    public static void markDirty(@NonNull Team team) {
        MinecraftServer server = GameInstance.getServer();
        boolean serverSide = Platform.getEnvironment() == Env.SERVER || (server != null && server.isSameThread());
        if (!serverSide) {
            if (MARK_DIRTY_WARNED.compareAndSet(false, true)) ManasCoreTeam.LOG.warn("TeamAPI.markDirty called on the client, ignoring.");
            return;
        }
        if (server == null) return;
        markDirty(server, team);
    }

    // Group operations, server only
    /**
     * Used to create a team of the type that is owned by the entity.
     *
     * @param type  Affected {@link TeamType} with the {@code GROUP} shape.
     * @param owner Affected {@link LivingEntity} that owns the team.
     * @return the new team, empty if it could not be created.
     * @see #tryCreateTeam(TeamType, LivingEntity)
     */
    public static <T extends Team> Optional<T> createTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner) {
        return TeamManager.createTeam(type, owner);
    }

    /**
     * Like {@link #createTeam(TeamType, LivingEntity)}, with the reason.
     */
    public static <T extends Team> TeamResult tryCreateTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner) {
        return TeamManager.tryCreateTeam(type, owner);
    }

    /**
     * Used to create a team of the type that is owned by the entity, with a custom name.
     *
     * @see #tryCreateTeam(TeamType, LivingEntity, String)
     */
    public static <T extends Team> Optional<T> createTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner, @Nullable String name) {
        return TeamManager.createTeam(type, owner, name);
    }

    /**
     * Like {@link #createTeam(TeamType, LivingEntity, String)}, with the reason.
     */
    public static <T extends Team> TeamResult tryCreateTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner, @Nullable String name) {
        return TeamManager.tryCreateTeam(type, owner, name);
    }

    /**
     * Used to get the team of the type with the given name, ignoring case.
     * This is server only.
     *
     * @param name Name of the team, which is sanitized before the lookup.
     */
    public static <T extends Team> Optional<T> getTeamByName(@NonNull MinecraftServer server, @NonNull TeamType<T> type, @NonNull String name) {
        ResourceLocation typeId = type.getId();
        if (typeId == null) return Optional.empty();
        String sanitized = TeamManager.sanitizeName(name);
        if (sanitized == null) return Optional.empty();
        TeamSavedData data = TeamSavedData.get(server);
        UUID id = data.getTeamByName(typeId, sanitized);
        if (id == null) return Optional.empty();
        return data.getTeam(id).filter(type.getTeamClass()::isInstance).map(type.getTeamClass()::cast);
    }

    /**
     * Used to disband the team and remove all of its members.
     *
     * @see #tryDisbandTeam
     */
    public static boolean disbandTeam(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.disbandTeam(server, team);
    }

    /**
     * Like {@link #disbandTeam}, with the reason.
     */
    public static TeamResult tryDisbandTeam(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.tryDisbandTeam(server, team);
    }

    /**
     * Used to add the entity to the team.
     * This bypasses invites, but respects limits, {@link TeamType#canJoin} and {@link TeamEvents#MEMBER_JOIN}.
     *
     * @see #tryAddMember
     */
    public static boolean addMember(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.addMember(team, entity);
    }

    /**
     * Like {@link #addMember}, with the reason.
     * Under {@link LimitPolicy#LEAVE_OLDEST} the evictions are applied before the final team check, so {@link TeamResult#NOT_FOUND} can be returned after the entity already left older teams.
     */
    public static TeamResult tryAddMember(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.tryAddMember(team, entity);
    }

    /**
     * Used to remove the entity from the team.
     *
     * @see #tryRemoveMember(Team, LivingEntity, LeaveReason)
     */
    public static boolean removeMember(@NonNull Team team, @NonNull LivingEntity entity, @NonNull LeaveReason reason) {
        if (!TeamManager.isServer(entity, "removeMember")) return false;
        LivingEntity resolved = team.getType().resolveMember(entity);
        return TeamManager.removeMember(entity.getServer(), team, resolved.getUUID(), resolved, reason);
    }

    /**
     * Like {@link #removeMember(Team, LivingEntity, LeaveReason)}, with the reason.
     */
    public static TeamResult tryRemoveMember(@NonNull Team team, @NonNull LivingEntity entity, @NonNull LeaveReason reason) {
        if (!TeamManager.isServer(entity, "removeMember")) return TeamResult.CLIENT_SIDE;
        LivingEntity resolved = team.getType().resolveMember(entity);
        return TeamManager.tryRemoveMember(entity.getServer(), team, resolved.getUUID(), resolved, reason);
    }

    /**
     * Used to remove an offline or unloaded member by id.
     *
     * @see #tryRemoveMember(MinecraftServer, Team, UUID, LeaveReason)
     */
    public static boolean removeMember(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID memberId, @NonNull LeaveReason reason) {
        return TeamManager.removeMember(server, team, memberId, null, reason);
    }

    /**
     * Like {@link #removeMember(MinecraftServer, Team, UUID, LeaveReason)}, with the reason.
     */
    public static TeamResult tryRemoveMember(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID memberId, @NonNull LeaveReason reason) {
        return TeamManager.tryRemoveMember(server, team, memberId, null, reason);
    }

    /**
     * Used to make the entity the owner of the team, which it must already be a member of.
     *
     * @see #trySetOwner
     */
    public static boolean setOwner(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.setOwner(team, entity);
    }

    /**
     * Like {@link #setOwner}, with the reason.
     */
    public static TeamResult trySetOwner(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.trySetOwner(team, entity);
    }

    /**
     * Determine if the inviter can send invites, subject to {@link TeamType#canInvite(Team, LivingEntity)}.
     */
    public static boolean canInvite(@NonNull Team team, @NonNull LivingEntity inviter) {
        TeamType<Team> type = typeOf(team);
        return type.canInvite(team, type.resolveMember(inviter));
    }

    /**
     * Determine if the actor can revoke the invite of the invitee, subject to {@link TeamType#canRevokeInvite}.
     */
    public static boolean canRevokeInvite(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID invitee) {
        TeamType<Team> type = typeOf(team);
        return type.canRevokeInvite(team, type.resolveMember(actor), invitee);
    }

    /**
     * Determine if the actor can kick the target member, subject to {@link TeamType#canKick}.
     */
    public static boolean canKick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        TeamType<Team> type = typeOf(team);
        return type.canKick(team, type.resolveMember(actor), target);
    }

    /**
     * Determine if the actor can promote the target member, subject to {@link TeamType#canPromote}.
     */
    public static boolean canPromote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        TeamType<Team> type = typeOf(team);
        return type.canPromote(team, type.resolveMember(actor), target);
    }

    /**
     * Determine if the actor can leave the team, subject to {@link TeamType#canLeave}.
     */
    public static boolean canLeave(@NonNull Team team, @NonNull LivingEntity actor) {
        TeamType<Team> type = typeOf(team);
        return type.canLeave(team, type.resolveMember(actor));
    }

    /**
     * Determine if the actor can disband the team, subject to {@link TeamType#canDisband}.
     */
    public static boolean canDisband(@NonNull Team team, @NonNull LivingEntity actor) {
        TeamType<Team> type = typeOf(team);
        return type.canDisband(team, type.resolveMember(actor));
    }

    /**
     * Determine if the actor can rename the team, subject to {@link TeamType#canRename}.
     */
    public static boolean canRename(@NonNull Team team, @NonNull LivingEntity actor) {
        TeamType<Team> type = typeOf(team);
        return type.canRename(team, type.resolveMember(actor));
    }

    /**
     * Used to kick the target member, subject to {@link TeamType#canKick}.
     */
    public static TeamResult tryKick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.tryKick(team, actor, target);
    }

    /**
     * Like {@link #tryKick}, collapsed to a boolean.
     */
    public static boolean kick(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.kick(team, actor, target);
    }

    /**
     * Used to promote the target member to owner, subject to {@link TeamType#canPromote}.
     */
    public static TeamResult tryPromote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.tryPromote(team, actor, target);
    }

    /**
     * Like {@link #tryPromote}, collapsed to a boolean.
     */
    public static boolean promote(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID target) {
        return TeamManager.promote(team, actor, target);
    }

    /**
     * Used to remove the actor from the team, subject to {@link TeamType#canLeave}.
     */
    public static TeamResult tryLeave(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.tryLeave(team, actor);
    }

    /**
     * Like {@link #tryLeave}, collapsed to a boolean.
     */
    public static boolean leave(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.leave(team, actor);
    }

    /**
     * Used to disband the team, subject to {@link TeamType#canDisband}.
     */
    public static TeamResult tryDisband(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.tryDisband(team, actor);
    }

    /**
     * Like {@link #tryDisband}, collapsed to a boolean.
     */
    public static boolean disband(@NonNull Team team, @NonNull LivingEntity actor) {
        return TeamManager.disband(team, actor);
    }

    /**
     * Used to rename the team when the actor can rename it.
     * The name is sanitized, {@link TeamType#isValidName} decides validity and types with {@link TeamType#requiresUniqueNames()} result in {@link TeamResult#NAME_TAKEN} for a taken name.
     *
     * @see #trySetTeamName(Team, LivingEntity, String)
     */
    public static boolean setTeamName(@NonNull Team team, @NonNull LivingEntity actor, @Nullable String name) {
        return TeamManager.setTeamName(team, actor, name);
    }

    /**
     * Like {@link #setTeamName(Team, LivingEntity, String)}, with the reason.
     */
    public static TeamResult trySetTeamName(@NonNull Team team, @NonNull LivingEntity actor, @Nullable String name) {
        return TeamManager.trySetTeamName(team, actor, name);
    }

    /**
     * Used to rename the team as the server, which bypasses {@link TeamType#canRename}.
     *
     * @see #trySetTeamName(MinecraftServer, Team, String)
     */
    public static boolean setTeamName(@NonNull MinecraftServer server, @NonNull Team team, @Nullable String name) {
        return TeamManager.setTeamName(server, team, name);
    }

    /**
     * Like {@link #setTeamName(MinecraftServer, Team, String)}, with the reason.
     */
    public static TeamResult trySetTeamName(@NonNull MinecraftServer server, @NonNull Team team, @Nullable String name) {
        return TeamManager.trySetTeamName(server, team, name);
    }

    /**
     * Used to invite the invitee into the team, subject to {@link TeamType#canInvite(Team, LivingEntity, LivingEntity)}, limits and {@link TeamEvents#INVITE_SEND}.
     *
     * @return the new invite, empty if it could not be created.
     * @see #tryInvite(Team, LivingEntity, LivingEntity)
     */
    public static Optional<TeamInvite> invite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull LivingEntity invitee) {
        return TeamManager.invite(team, inviter, invitee);
    }

    /**
     * Like {@link #invite(Team, LivingEntity, LivingEntity)}, with the reason.
     */
    public static TeamResult tryInvite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull LivingEntity invitee) {
        return TeamManager.tryInvite(team, inviter, invitee);
    }

    /**
     * Used to invite an invitee by id into the team, who can be offline.
     * This is subject to {@link TeamType#canInvite(Team, LivingEntity, UUID)}.
     *
     * @see #tryInvite(Team, LivingEntity, UUID)
     */
    public static Optional<TeamInvite> invite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull UUID invitee) {
        return TeamManager.invite(team, inviter, invitee);
    }

    /**
     * Like {@link #invite(Team, LivingEntity, UUID)}, with the reason.
     */
    public static TeamResult tryInvite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull UUID invitee) {
        return TeamManager.tryInvite(team, inviter, invitee);
    }

    /**
     * Used to invite an invitee as the server, which skips the permission hooks of the {@link TeamType}.
     * A null inviter is a system invite and is stored as {@link net.minecraft.Util#NIL_UUID}.
     *
     * @see #tryInvite(MinecraftServer, Team, UUID, UUID)
     */
    public static Optional<TeamInvite> invite(@NonNull MinecraftServer server, @NonNull Team team, @Nullable UUID inviter, @NonNull UUID invitee) {
        return TeamManager.invite(server, team, inviter, invitee);
    }

    /**
     * Like {@link #invite(MinecraftServer, Team, UUID, UUID)}, with the reason.
     */
    public static TeamResult tryInvite(@NonNull MinecraftServer server, @NonNull Team team, @Nullable UUID inviter, @NonNull UUID invitee) {
        return TeamManager.tryInvite(server, team, inviter, invitee);
    }

    /**
     * Used to accept the invite of the invitee into the team.
     *
     * @return false if the invite does not exist, the type is unknown or the join was rejected.
     * @see #tryAcceptInvite
     */
    public static boolean acceptInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.acceptInvite(invitee, teamId);
    }

    /**
     * Like {@link #acceptInvite}, with the reason.
     * An invite of an unregistered type is removed and synced before {@link TeamResult#NOT_FOUND} is returned.
     * The join result, for example {@link TeamResult#TEAM_FULL}, is propagated as-is.
     */
    public static TeamResult tryAcceptInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.tryAcceptInvite(invitee, teamId);
    }

    /**
     * Used to decline the invite of the invitee into the team.
     *
     * @see #tryDeclineInvite
     */
    public static boolean declineInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.declineInvite(invitee, teamId);
    }

    /**
     * Like {@link #declineInvite}, with the reason.
     */
    public static TeamResult tryDeclineInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.tryDeclineInvite(invitee, teamId);
    }

    /**
     * Used to revoke the invite of the invitee into the team, subject to {@link TeamType#canRevokeInvite}.
     *
     * @see #tryRevokeInvite(Team, LivingEntity, UUID)
     */
    public static boolean revokeInvite(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID invitee) {
        return TeamManager.revokeInvite(team, actor, invitee);
    }

    /**
     * Like {@link #revokeInvite(Team, LivingEntity, UUID)}, with the reason.
     */
    public static TeamResult tryRevokeInvite(@NonNull Team team, @NonNull LivingEntity actor, @NonNull UUID invitee) {
        return TeamManager.tryRevokeInvite(team, actor, invitee);
    }

    /**
     * Used to revoke the invite of the invitee into the team as the server, which skips {@link TeamType#canRevokeInvite}.
     *
     * @see #tryRevokeInvite(MinecraftServer, Team, UUID)
     */
    public static boolean revokeInvite(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID invitee) {
        return TeamManager.revokeInvite(server, team, invitee);
    }

    /**
     * Like {@link #revokeInvite(MinecraftServer, Team, UUID)}, with the reason.
     */
    public static TeamResult tryRevokeInvite(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID invitee) {
        return TeamManager.tryRevokeInvite(server, team, invitee);
    }

    /**
     * Used to get the online players this team can still invite, with every limit and hook applied.
     * Players with a pending invite are excluded, see {@link #getOutgoingInvites}.
     * This is server only.
     */
    public static List<ServerPlayer> getInvitable(@NonNull Team team, @NonNull ServerPlayer requester) {
        return TeamManager.getInvitable(team, requester);
    }

    /**
     * Used to get the ids of the players this team can still invite.
     * On the client this is the last list received for {@link Client#requestInvitable}, on the server it is computed now.
     */
    public static List<UUID> getInvitable(@NonNull LivingEntity viewer, @NonNull UUID teamId) {
        if (viewer.level().isClientSide()) return ClientTeamCache.getInvitable(teamId);
        MinecraftServer server = viewer.getServer();
        if (server == null || !(viewer instanceof ServerPlayer player)) return List.of();
        Optional<Team> team = getTeam(server, teamId);
        if (team.isEmpty()) return List.of();
        return getInvitable(team.get(), player).stream().map(ServerPlayer::getUUID).toList();
    }

    /**
     * Used to get the pending invites sent by this team.
     * This is server only.
     */
    public static List<TeamInvite> getOutgoingInvites(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.getOutgoingInvites(server, team);
    }

    /**
     * Used to get the pending invites sent by this team.
     * On the server this reads the saved table, on the client the local cache, which only holds the teams of the local player.
     */
    public static List<TeamInvite> getOutgoingInvites(@NonNull LivingEntity viewer, @NonNull Team team) {
        if (!viewer.level().isClientSide()) return TeamManager.getOutgoingInvites(viewer.getServer(), team);
        return ClientTeamCache.getOutgoingInvites(team.getId());
    }

    /**
     * Determine if the given player already has a pending invite into this team.
     * This is server only.
     */
    public static boolean hasPendingInvite(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID invitee) {
        return TeamManager.hasPendingInvite(server, team, invitee);
    }

    // Relation operations, server only
    /**
     * Used to relate {@code a} to {@code b} under the type, which must have the {@code RELATION} shape.
     * Both entities are resolved through the ownership rules of the type.
     *
     * @see #tryAddRelation(TeamType, LivingEntity, LivingEntity)
     */
    public static boolean addRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.addRelation(type, a, b);
    }

    /**
     * Like {@link #addRelation(TeamType, LivingEntity, LivingEntity)}, with the reason.
     */
    public static TeamResult tryAddRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.tryAddRelation(type, a, b);
    }

    /**
     * Used to remove the relation of {@code a} to {@code b} under the type.
     * Both entities are resolved through the ownership rules of the type.
     *
     * @see #tryRemoveRelation(TeamType, LivingEntity, LivingEntity)
     */
    public static boolean removeRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.removeRelation(type, a, b);
    }

    /**
     * Like {@link #removeRelation(TeamType, LivingEntity, LivingEntity)}, with the reason.
     */
    public static TeamResult tryRemoveRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.tryRemoveRelation(type, a, b);
    }

    /**
     * Determine if {@code a} is related to {@code b} under the type.
     * For symmetric types the relation counts in both directions.
     */
    public static boolean hasRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.hasRelation(type, a, b);
    }

    /**
     * Used to get the ids of the entities that list this one under the given {@code RELATION} type.
     * This works on both sides for tracked entities.
     */
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
     * Used to relate {@code a} to {@code b} under the type by id.
     * Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated on their next login, until then their old relations are not visible to this method.
     *
     * @see #tryAddRelation(MinecraftServer, TeamType, UUID, UUID)
     */
    public static boolean addRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.addRelation(server, type, a, b);
    }

    /**
     * Like {@link #addRelation(MinecraftServer, TeamType, UUID, UUID)}, with the reason.
     */
    public static TeamResult tryAddRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.tryAddRelation(server, type, a, b);
    }

    /**
     * Used to remove the relation of {@code a} to {@code b} under the type by id.
     * Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated on their next login, until then their old relations are not visible to this method.
     *
     * @see #tryRemoveRelation(MinecraftServer, TeamType, UUID, UUID)
     */
    public static boolean removeRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.removeRelation(server, type, a, b);
    }

    /**
     * Like {@link #removeRelation(MinecraftServer, TeamType, UUID, UUID)}, with the reason.
     */
    public static TeamResult tryRemoveRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.tryRemoveRelation(server, type, a, b);
    }

    /**
     * Determine if {@code a} is related to {@code b} under the type by id.
     * Works for offline or unloaded entities.
     * Players who have not logged in since the relation table was introduced are migrated on their next login, until then their old relations are not visible to this method.
     */
    public static boolean hasRelation(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID a, @NonNull UUID b) {
        return TeamManager.hasRelation(server, type, a, b);
    }

    /**
     * Used to get the ids of the entities that {@code entity} is related to under the type.
     * The result is unmodifiable.
     */
    public static Set<UUID> getRelated(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID entity) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(TeamManager.getRelated(server, type, entity)));
    }

    /**
     * Server side, id based counterpart of {@link #getRelatedBy(LivingEntity, TeamType)}.
     */
    public static Set<UUID> getRelatedBy(@NonNull MinecraftServer server, @NonNull TeamType<?> type, @NonNull UUID entity) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(TeamManager.getRelatedBy(server, type, entity)));
    }

    /**
     * Client side senders for {@link TeamActionPayload}.
     * Every mutation goes through the {@code try*} handlers of the server, which answers with a {@link TeamResult} that fires {@link TeamEvents#CLIENT_ACTION_RESULT}.
     * Calling these on the server thread logs a warning once and does nothing.
     */
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

        /**
         * Used to ask the server to create a team of the type.
         */
        public static void create(@NonNull TeamType<?> type, @Nullable String name) {
            if (guard("create")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.CREATE, type.getId(), null, null, clamp(name)));
        }

        /**
         * Used to ask the server to rename the team.
         */
        public static void rename(@NonNull UUID teamId, @Nullable String name) {
            if (guard("rename")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.RENAME, null, teamId, null, clamp(name)));
        }

        /**
         * Used to ask the server to invite a player into the team.
         */
        public static void invite(@NonNull UUID teamId, @NonNull UUID invitee) {
            if (guard("invite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.INVITE, null, teamId, invitee, null));
        }

        /**
         * Used to ask the server to kick a member from the team.
         */
        public static void kick(@NonNull UUID teamId, @NonNull UUID member) {
            if (guard("kick")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.KICK, null, teamId, member, null));
        }

        /**
         * Used to ask the server to promote a member of the team to owner.
         */
        public static void promote(@NonNull UUID teamId, @NonNull UUID member) {
            if (guard("promote")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.PROMOTE, null, teamId, member, null));
        }

        /**
         * Used to ask the server to leave the team.
         */
        public static void leave(@NonNull UUID teamId) {
            if (guard("leave")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.LEAVE, null, teamId, null, null));
        }

        /**
         * Used to ask the server to disband the team.
         */
        public static void disband(@NonNull UUID teamId) {
            if (guard("disband")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.DISBAND, null, teamId, null, null));
        }

        /**
         * Used to ask the server to accept the invite into the team.
         */
        public static void acceptInvite(@NonNull UUID teamId) {
            if (guard("acceptInvite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.ACCEPT_INVITE, null, teamId, null, null));
        }

        /**
         * Used to ask the server to decline the invite into the team.
         */
        public static void declineInvite(@NonNull UUID teamId) {
            if (guard("declineInvite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.DECLINE_INVITE, null, teamId, null, null));
        }

        /**
         * Used to ask the server to revoke the invite of a player into the team.
         */
        public static void revokeInvite(@NonNull UUID teamId, @NonNull UUID invitee) {
            if (guard("revokeInvite")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.REVOKE_INVITE, null, teamId, invitee, null));
        }

        /**
         * Used to ask the server to relate the player to the target under the type.
         */
        public static void addRelation(@NonNull TeamType<?> type, @NonNull UUID target) {
            if (guard("addRelation")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.ADD_RELATION, type.getId(), null, target, null));
        }

        /**
         * Used to ask the server to remove the relation of the player to the target under the type.
         */
        public static void removeRelation(@NonNull TeamType<?> type, @NonNull UUID target) {
            if (guard("removeRelation")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.REMOVE_RELATION, type.getId(), null, target, null));
        }

        /**
         * Used to ask the server for the players the team can still invite.
         * The answer is available through {@link TeamAPI#getInvitable(LivingEntity, UUID)} and {@link TeamEvents#CLIENT_INVITABLE_UPDATED}.
         */
        public static void requestInvitable(@NonNull UUID teamId) {
            if (guard("requestInvitable")) return;
            NetworkManager.sendToServer(new TeamActionPayload(TeamAction.REQUEST_INVITABLE, null, teamId, null, null));
        }
    }
}
