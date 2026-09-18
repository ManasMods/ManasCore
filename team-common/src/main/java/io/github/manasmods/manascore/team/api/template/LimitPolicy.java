/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import io.github.manasmods.manascore.team.api.TeamType;

/**
 * What happens when an entity joins a team of a type it already holds {@link TeamType#maxTeamsPerMember()} of.
 */
public enum LimitPolicy {
    REJECT,
    LEAVE_OLDEST
}
