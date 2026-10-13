/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

/**
 * Reason why an entity left a team.
 * <p>
 * It is passed to {@link TeamEvents#MEMBER_LEAVE}, {@link TeamEvents#MEMBER_LEFT} and the removal methods of the {@code TeamAPI}.
 */
public enum LeaveReason {
    /**
     * The member left on its own.
     */
    LEAVE,
    /**
     * The member was kicked by another member.
     */
    KICK,
    /**
     * The member died. Only entities that are not players are removed from their teams on death.
     */
    DEATH,
    /**
     * The team was disbanded, which removes every member.
     */
    DISBAND
}
