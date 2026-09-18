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
import net.minecraft.world.entity.LivingEntity;

/**
 * Folds every registered {@link TeamType} into one answer, highest priority first.
 * Works on both sides because it only reads the synced entity storage.
 */
public class RelationResolver {
    private RelationResolver() {
    }

    public static Teams teamsOf(LivingEntity entity) {
        Teams teams = entity.manasCore$getStorage(TeamStorage.getKey());
        return teams == null ? Teams.EMPTY : teams;
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

        for (TeamType<?> type : TeamRegistry.sortedByPriority()) {
            LivingEntity ra = type.resolveMember(a);
            LivingEntity rb = type.resolveMember(b);
            if (ra.getUUID().equals(rb.getUUID())) return new ResolvedRelation(Relation.ALLY, type);

            Relation relation = type.getRelation(ra, teamsOf(ra), rb, teamsOf(rb));
            if (relation != Relation.NEUTRAL) return new ResolvedRelation(relation, type);
        }
        return ResolvedRelation.NEUTRAL;
    }
}
