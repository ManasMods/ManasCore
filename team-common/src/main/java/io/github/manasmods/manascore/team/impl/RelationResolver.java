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
 * Both sides are resolved through {@link OwnerResolvers} once per fold; a type that
 * {@link TeamType#inheritsOwnership()} sees the root owner, the others see the entity itself.
 * Works on both sides: it reads the entity storage of loaded entities and, on the server, the
 * saved table for owners that are offline or unloaded.
 */
public class RelationResolver {
    private RelationResolver() {
    }

    public static Teams teamsOf(LivingEntity entity) {
        Teams teams = entity.manasCore$getStorage(TeamStorage.getKey());
        return teams == null ? Teams.EMPTY : teams;
    }

    /** Team data of any id: the loaded entity's storage, else on the server the saved table, else empty. */
    public static Teams teamsOf(Level level, UUID id) {
        LivingEntity loaded = OwnerResolvers.findLoaded(level, id);
        if (loaded != null) return teamsOf(loaded);
        MinecraftServer server = level.getServer();
        if (level.isClientSide() || server == null) return Teams.EMPTY;
        return new SavedDataTeams(TeamSavedData.get(server), id);
    }

    public static ResolvedRelation resolve(LivingEntity a, LivingEntity b) {
        ResolvedRelation result = fold(a, b);

        Changeable<Relation> override = Changeable.of(result.relation());
        TeamEvents.RESOLVE_RELATION.invoker().resolve(a, b, override);
        if (override.hasChanged() && override.isPresent()) return new ResolvedRelation(override.get(), null);
        return result;
    }

    private static ResolvedRelation fold(LivingEntity a, LivingEntity b) {
        if (a == b || a.getUUID().equals(b.getUUID())) return ResolvedRelation.SELF;
        UUID ownerA = OwnerResolvers.resolveId(a);
        UUID ownerB = OwnerResolvers.resolveId(b);
        Teams ownA = teamsOf(a);
        Teams ownB = teamsOf(b);
        Teams ownerTeamsA = ownerA.equals(a.getUUID()) ? ownA : teamsOf(a.level(), ownerA);
        Teams ownerTeamsB = ownerB.equals(b.getUUID()) ? ownB : teamsOf(b.level(), ownerB);

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
