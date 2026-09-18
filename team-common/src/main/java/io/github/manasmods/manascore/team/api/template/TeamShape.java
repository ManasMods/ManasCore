/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import io.github.manasmods.manascore.team.api.Team;

/**
 * GROUP: shared {@link Team} object with identity, members reference it by id.
 * RELATION: no shared object, each entity stores the ids of entities it is related to.
 */
public enum TeamShape {
    GROUP,
    RELATION
}
