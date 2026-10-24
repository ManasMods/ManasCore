/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import io.github.manasmods.manascore.team.api.template.Relation;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import org.jetbrains.annotations.Nullable;

/**
 * Folded relation plus the {@link TeamType} that decided it.
 * <p>
 * {@code decidedBy} is null for NEUTRAL, for the self check and when {@link TeamEvents#RESOLVE_RELATION} overrode the result.
 * {@link #isOwnership()} is true when both sides resolve to the same root owner. {@code decidedBy} is then null and {@link TeamConfig#ownership} applies.
 *
 * @param relation  Folded {@link Relation} of the check.
 * @param decidedBy {@link TeamType} that decided the relation, null if none did.
 * @param ownership true if both sides resolve to the same root owner.
 */
public record ResolvedRelation(Relation relation, @Nullable TeamType<?> decidedBy, boolean ownership) {
    /**
     * Result when no team type decided on a relation.
     */
    public static final ResolvedRelation NEUTRAL = new ResolvedRelation(Relation.NEUTRAL, null);
    /**
     * Result of the check of an entity against itself.
     */
    public static final ResolvedRelation SELF = new ResolvedRelation(Relation.ALLY, null);
    /**
     * Result when both sides resolve to the same root owner.
     */
    public static final ResolvedRelation OWNERSHIP = new ResolvedRelation(Relation.ALLY, null, true);

    public ResolvedRelation(Relation relation, @Nullable TeamType<?> decidedBy) {
        this(relation, decidedBy, false);
    }

    /**
     * Determine if the folded relation is {@link Relation#ALLY}.
     */
    public boolean isAlly() {
        return this.relation == Relation.ALLY;
    }

    /**
     * Determine if the folded relation is {@link Relation#ENEMY}.
     */
    public boolean isEnemy() {
        return this.relation == Relation.ENEMY;
    }

    /**
     * Determine if both sides resolve to the same root owner.
     */
    public boolean isOwnership() {
        return this.ownership;
    }
}
