/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

/**
 * Result of a relation check between two entities.
 * The relation is read from the first entity towards the second one.
 */
public enum Relation {
    /**
     * The first entity considers the second one an ally.
     */
    ALLY,
    /**
     * No team type decided on a relation.
     */
    NEUTRAL,
    /**
     * The first entity considers the second one an enemy.
     */
    ENEMY
}
