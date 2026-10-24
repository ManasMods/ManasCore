/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

/**
 * Kinds of team mutations that a client can request from the server, carried by {@code TeamActionPayload}.
 * <p>
 * The server answers every request with a {@link TeamResult}, which the client receives through {@link TeamEvents#CLIENT_ACTION_RESULT}.
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
    /**
     * Asks for the list of players that a team may still invite.
     */
    REQUEST_INVITABLE,
    REVOKE_INVITE
}
