/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Outcome of a team mutation, returned by the {@code tryX} methods of {@code TeamManager} and {@code TeamAPI}.
 * <p>
 * The boolean and {@link java.util.Optional} variants of those methods delegate to them and collapse the result to {@link #isAccepted()}.
 */
public enum TeamResult {
    /**
     * The mutation was applied.
     */
    ACCEPTED,
    /**
     * The mutation was requested on the client. Team mutations are server only.
     */
    CLIENT_SIDE,
    /**
     * The team, team type, invite or entity storage of the request could not be found.
     */
    NOT_FOUND,
    /**
     * The shape of the team type does not support the operation, for example a relation on a {@code GROUP} type.
     */
    WRONG_SHAPE,
    /**
     * The team type refused the actor, see the {@code canX} methods of {@code TeamType}.
     */
    NOT_ALLOWED,
    /**
     * The entity is not a member of the team.
     */
    NOT_MEMBER,
    /**
     * The entity is already a member of the team.
     */
    ALREADY_MEMBER,
    /**
     * The team reached the maximum number of members of its type.
     */
    TEAM_FULL,
    /**
     * The entity reached the maximum number of teams of the type, or of relations under the type.
     */
    LIMIT_REACHED,
    /**
     * The invitee already has a pending invite into the team.
     */
    INVITE_PENDING,
    /**
     * An argument was not valid, for example a name the type rejects or a relation of an entity with itself.
     */
    INVALID,
    /**
     * The mutation would not have changed anything.
     */
    UNCHANGED,
    /**
     * A {@link TeamEvents} listener cancelled the mutation.
     */
    CANCELLED,
    /**
     * The name is already used by another team of a type that requires unique names.
     */
    NAME_TAKEN;

    /**
     * Determine if the mutation was applied.
     */
    public boolean isAccepted() {
        return this == ACCEPTED;
    }

    /**
     * Used to get the translation key of this result.
     */
    public String getTranslationKey() {
        return "manascore.team.result." + this.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Used to get the translated {@link Component} name of this result.
     */
    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }
}
