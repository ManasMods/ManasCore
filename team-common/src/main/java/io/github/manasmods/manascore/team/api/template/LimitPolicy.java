/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import io.github.manasmods.manascore.team.api.TeamType;

/**
 * Determine what happens when an entity joins a team of a type it already holds {@link TeamType#getMaxTeamsPerMember()} of.
 *
 * @see TeamType#onLimitReached()
 */
public enum LimitPolicy {
    /**
     * The join is rejected with {@link TeamResult#LIMIT_REACHED}.
     */
    REJECT,
    /**
     * The oldest teams of the type are left to make room for the new one.
     * The automatic leave skips {@link TeamEvents#MEMBER_LEAVE}, so it can not be cancelled.
     */
    LEAVE_OLDEST
}
