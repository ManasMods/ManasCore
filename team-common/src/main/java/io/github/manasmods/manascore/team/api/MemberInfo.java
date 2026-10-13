/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api;

import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Snapshot of a team member's identity for display purposes.
 *
 * @param id     Id of the member.
 * @param name   Profile name of the member, the last known one if the member is offline.
 * @param online true if the member is currently connected.
 */
public record MemberInfo(UUID id, Component name, boolean online) {
}
