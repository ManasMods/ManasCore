/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import io.github.manasmods.manascore.network.api.util.Changeable;
import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.api.template.Teams;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Folds every registered {@link TeamType} into one answer, highest priority first.
 * Both sides are resolved through {@link OwnerResolvers} once per fold. A type that {@link TeamType#inheritsOwnership()} sees the root owner, the others see the entity itself.
 * <p>
 * When both sides share a root owner the result is {@link ResolvedRelation#OWNERSHIP} before any type is consulted.
 * <p>
 * Works on both sides: it reads the entity storage of loaded entities and, on the server, the saved table for owners that are offline or unloaded.
 */
public class RelationResolver {
    private RelationResolver() {
    }

    /**
     * Used to get the team data of the loaded entity, or {@link Teams#EMPTY} if it has no storage.
     */
    public static Teams teamsOf(LivingEntity entity) {
        Teams teams = entity.manasCore$getStorage(TeamStorage.getKey());
        return teams == null ? Teams.EMPTY : teams;
    }

    /**
     * Used to get the team data of any id: the storage of the loaded entity, else on the server the saved table, else empty.
     */
    public static Teams teamsOf(Level level, UUID id) {
        LivingEntity loaded = OwnerResolvers.findLoaded(level, id);
        if (loaded != null) return teamsOf(loaded);
        MinecraftServer server = level.getServer();
        if (level.isClientSide() || server == null) return Teams.EMPTY;
        return new SavedDataTeams(TeamSavedData.get(server), id);
    }

    /**
     * Used to resolve the relation of {@code a} towards {@code b}.
     * The folded result can be overridden by a listener of {@link TeamEvents#RESOLVE_RELATION}.
     */
    public static ResolvedRelation resolve(LivingEntity a, LivingEntity b) {
        // Fold team types
        ResolvedRelation result = fold(a, b);

        // Let listeners override
        Changeable<Relation> override = Changeable.of(result.relation());
        TeamEvents.RESOLVE_RELATION.invoker().resolve(a, b, override);
        if (override.hasChanged() && override.isPresent()) return new ResolvedRelation(override.get(), null);
        return result;
    }

    private static ResolvedRelation fold(LivingEntity a, LivingEntity b) {
        // Same entity
        if (a == b || a.getUUID().equals(b.getUUID())) return ResolvedRelation.SELF;

        // Same root owner
        UUID ownerA = OwnerResolvers.resolveId(a);
        UUID ownerB = OwnerResolvers.resolveId(b);
        if (ownerA.equals(ownerB)) return ResolvedRelation.OWNERSHIP;

        // Shortcut for entities without owner or team data
        Teams ownA = teamsOf(a);
        Teams ownB = teamsOf(b);
        if (ownerA.equals(a.getUUID()) && ownerB.equals(b.getUUID()) && ownA.isEmpty() && ownB.isEmpty()) return ResolvedRelation.NEUTRAL;

        // Load team data of the owners
        Teams ownerTeamsA = ownerA.equals(a.getUUID()) ? ownA : teamsOf(a.level(), ownerA);
        Teams ownerTeamsB = ownerB.equals(b.getUUID()) ? ownB : teamsOf(b.level(), ownerB);

        // Ask every type by priority
        for (TeamType<?> type : TeamRegistry.sortedByPriority()) {
            boolean inherit = type.inheritsOwnership();
            UUID ra = inherit ? ownerA : a.getUUID();
            UUID rb = inherit ? ownerB : b.getUUID();
            if (ra.equals(rb)) return new ResolvedRelation(Relation.ALLY, type);

            Teams ta = inherit ? ownerTeamsA : ownA;
            Teams tb = inherit ? ownerTeamsB : ownB;
            if (ta.isEmpty() && tb.isEmpty()) continue;

            Relation relation = type.getRelation(a.level(), ra, ta, rb, tb);
            if (relation != Relation.NEUTRAL) return new ResolvedRelation(relation, type);
        }
        return ResolvedRelation.NEUTRAL;
    }
}
