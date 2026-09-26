/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Outcome of a team mutation, returned by the {@code tryX} methods on
 * {@code TeamManager}/{@code TeamAPI}. The boolean/{@code Optional} variants of those
 * methods delegate to these and collapse the result to {@link #isAccepted()}.
 */
public enum TeamResult {
    ACCEPTED,
    CLIENT_SIDE,
    NOT_FOUND,
    WRONG_SHAPE,
    NOT_ALLOWED,
    NOT_MEMBER,
    ALREADY_MEMBER,
    TEAM_FULL,
    LIMIT_REACHED,
    INVITE_PENDING,
    INVALID,
    UNCHANGED,
    CANCELLED;

    public boolean isAccepted() {
        return this == ACCEPTED;
    }

    public String getTranslationKey() {
        return "manascore.team.result." + this.name().toLowerCase(Locale.ROOT);
    }

    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }
}
