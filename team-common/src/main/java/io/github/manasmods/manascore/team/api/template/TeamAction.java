/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

/**
 * Client-to-server team mutation kinds, carried by {@code TeamActionPayload}.
 */
public enum TeamAction {
    CREATE,
    RENAME,
    INVITE,
    KICK,
    PROMOTE,
    LEAVE,
    DISBAND,
    ACCEPT_INVITE,
    DECLINE_INVITE,
    ADD_RELATION,
    REMOVE_RELATION,
    REQUEST_INVITABLE
}
