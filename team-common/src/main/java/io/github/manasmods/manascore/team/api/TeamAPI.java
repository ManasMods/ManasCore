/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.api.template.LeaveReason;
import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.api.template.Teams;
import io.github.manasmods.manascore.team.impl.RelationResolver;
import io.github.manasmods.manascore.team.impl.TeamManager;
import io.github.manasmods.manascore.team.impl.TeamRegistry;
import io.github.manasmods.manascore.team.impl.TeamSavedData;
import io.github.manasmods.manascore.team.impl.TeamStorage;
import io.github.manasmods.manascore.team.impl.builtin.AllyTeamType;
import io.github.manasmods.manascore.team.impl.builtin.PartyTeamType;
import io.github.manasmods.manascore.team.impl.client.ClientTeamCache;
import lombok.NonNull;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

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

    public static ResolvedRelation resolveRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return RelationResolver.resolve(a, b);
    }

    public static Relation getRelation(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return resolveRelation(a, b).relation();
    }

    public static boolean isAllied(@NonNull LivingEntity a, @NonNull LivingEntity b) {
        return getRelation(a, b) == Relation.ALLY;
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

    public static <T extends Team> Optional<T> getTeam(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        Set<T> teams = getTeams(entity, type);
        return teams.isEmpty() ? Optional.empty() : Optional.of(teams.iterator().next());
    }

    public static <T extends Team> Set<T> getTeams(@NonNull LivingEntity entity, @NonNull TeamType<T> type) {
        LivingEntity resolved = type.resolveMember(entity);
        Set<T> result = new LinkedHashSet<>();
        for (UUID id : getTeamsFrom(resolved).getTeamIds(type)) {
            getTeam(resolved, id).filter(type.teamClass()::isInstance).map(type.teamClass()::cast).ifPresent(result::add);
        }
        return Collections.unmodifiableSet(result);
    }

    public static List<TeamInvite> getPendingInvites(@NonNull LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        if (server != null && !entity.level().isClientSide()) return TeamManager.getPendingInvites(server, entity.getUUID());
        return ClientTeamCache.getInvites();
    }

    /** Server only. Every team of every type. */
    public static Collection<Team> getAllTeams(@NonNull MinecraftServer server) {
        return TeamSavedData.get(server).getTeams();
    }

    /*** GROUP ops (server only) ***/
    public static <T extends Team> Optional<T> createTeam(@NonNull TeamType<T> type, @NonNull LivingEntity owner) {
        return TeamManager.createTeam(type, owner);
    }

    public static boolean disbandTeam(@NonNull MinecraftServer server, @NonNull Team team) {
        return TeamManager.disbandTeam(server, team);
    }

    /** Bypasses invites. Respects limits, {@link TeamType#canJoin} and {@link TeamEvents#MEMBER_JOIN}. */
    public static boolean addMember(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.addMember(team, entity);
    }

    public static boolean removeMember(@NonNull Team team, @NonNull LivingEntity entity, @NonNull LeaveReason reason) {
        if (!TeamManager.isServer(entity, "removeMember")) return false;
        LivingEntity resolved = team.getType().resolveMember(entity);
        return TeamManager.removeMember(entity.getServer(), team, resolved.getUUID(), resolved, reason);
    }

    /** Removes an offline or unloaded member by id. */
    public static boolean removeMember(@NonNull MinecraftServer server, @NonNull Team team, @NonNull UUID memberId, @NonNull LeaveReason reason) {
        return TeamManager.removeMember(server, team, memberId, null, reason);
    }

    public static boolean setOwner(@NonNull Team team, @NonNull LivingEntity entity) {
        return TeamManager.setOwner(team, entity);
    }

    public static Optional<TeamInvite> invite(@NonNull Team team, @NonNull LivingEntity inviter, @NonNull LivingEntity invitee) {
        return TeamManager.invite(team, inviter, invitee);
    }

    public static boolean acceptInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.acceptInvite(invitee, teamId);
    }

    public static boolean declineInvite(@NonNull LivingEntity invitee, @NonNull UUID teamId) {
        return TeamManager.declineInvite(invitee, teamId);
    }

    /*** RELATION ops (server only) ***/
    public static Optional<TeamInvite> requestRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.requestRelation(type, a, b);
    }

    public static boolean addRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.addRelation(type, a, b);
    }

    public static boolean removeRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.removeRelation(type, a, b);
    }

    public static boolean hasRelation(@NonNull TeamType<?> type, @NonNull LivingEntity a, @NonNull LivingEntity b) {
        return TeamManager.hasRelation(type, a, b);
    }
}
