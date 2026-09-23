/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Snapshot of a team member's identity for display purposes.
 */
public record MemberInfo(UUID id, Component name, boolean online) {
}
