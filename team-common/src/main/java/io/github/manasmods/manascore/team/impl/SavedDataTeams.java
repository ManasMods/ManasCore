/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.impl;

import io.github.manasmods.manascore.team.api.*;
import io.github.manasmods.manascore.team.api.template.Teams;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only {@link Teams} view of one id over {@link TeamSavedData}, for owners that are offline or not loaded. Server only. {@link #markDirty()} is a no-op.
 */
@RequiredArgsConstructor
public class SavedDataTeams implements Teams {
    private final TeamSavedData data;
    private final UUID id;

    public Set<UUID> getTeamIds(TeamType<?> type) {
        Set<UUID> ids = this.data.getTeamIdsOf(this.id);
        if (ids.isEmpty()) return Collections.emptySet();
        Set<UUID> result = new LinkedHashSet<>();
        for (UUID teamId : ids) {
            if (this.data.getTeam(teamId).map(team -> team.getType() == type).orElse(false)) result.add(teamId);
        }
        return Collections.unmodifiableSet(result);
    }

    public Map<ResourceLocation, Set<UUID>> getAllTeamIds() {
        Map<ResourceLocation, Set<UUID>> result = new LinkedHashMap<>();
        for (UUID teamId : this.data.getTeamIdsOf(this.id)) {
            this.data.getTeam(teamId).ifPresent(team -> result.computeIfAbsent(team.getType().getId(), k -> new LinkedHashSet<>()).add(teamId));
        }
        return result;
    }

    public Set<UUID> getRelated(TeamType<?> type) {
        ResourceLocation typeId = type.getId();
        return typeId == null ? Collections.emptySet() : this.data.getRelated(typeId, this.id);
    }

    public Map<ResourceLocation, Set<UUID>> getAllRelated() {
        return this.data.getAllRelated(this.id);
    }

    public Set<UUID> getRelatedBy(TeamType<?> type) {
        ResourceLocation typeId = type.getId();
        return typeId == null ? Collections.emptySet() : this.data.getRelatedBy(typeId, this.id);
    }

    public void markDirty() {
    }

    public boolean isEmpty() {
        return this.data.getTeamIdsOf(this.id).isEmpty() && !this.data.hasAnyRelation(this.id);
    }
}
