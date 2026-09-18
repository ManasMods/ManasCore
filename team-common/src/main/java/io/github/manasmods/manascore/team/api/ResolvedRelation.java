/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import org.jetbrains.annotations.Nullable;

/**
 * Folded relation plus the type that decided it. {@code decidedBy} is null for NEUTRAL,
 * for the self check, and when {@link TeamEvents#RESOLVE_RELATION} overrode the result.
 */
public record ResolvedRelation(Relation relation, @Nullable TeamType<?> decidedBy) {
    public static final ResolvedRelation NEUTRAL = new ResolvedRelation(Relation.NEUTRAL, null);
    public static final ResolvedRelation SELF = new ResolvedRelation(Relation.ALLY, null);

    public boolean isAlly() {
        return this.relation == Relation.ALLY;
    }

    public boolean isEnemy() {
        return this.relation == Relation.ENEMY;
    }
}
