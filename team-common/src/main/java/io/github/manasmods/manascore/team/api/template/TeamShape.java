/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import io.github.manasmods.manascore.team.api.Team;

/**
 * Determine how a {@link io.github.manasmods.manascore.team.api.TeamType} stores its data.
 *
 * @see io.github.manasmods.manascore.team.api.TeamType#getShape()
 */
public enum TeamShape {
    /**
     * Shared {@link Team} object with identity, members reference it by id.
     */
    GROUP,
    /**
     * No shared object, each entity stores the ids of entities it is related to.
     */
    RELATION
}
