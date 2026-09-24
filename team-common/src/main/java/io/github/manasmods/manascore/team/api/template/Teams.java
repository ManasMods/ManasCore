/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.api.template;

import io.github.manasmods.manascore.team.api.TeamAPI;
import io.github.manasmods.manascore.team.api.TeamType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Entity-side view of team membership. Implemented by the team storage and, on the server, by a
 * read-only view over the saved table for ids that are not loaded. Reads are safe on both sides;
 * the storage is synced to its holder only.
 */
public interface Teams {
    /**
     * No-op fallback used by {@link TeamAPI#getTeamsFrom} when an entity has no team storage.
     */
    Teams EMPTY = new Teams() {
        public Set<UUID> getTeamIds(TeamType<?> type) {
            return Collections.emptySet();
        }
        public Map<ResourceLocation, Set<UUID>> getAllTeamIds() {
            return new HashMap<>();
        }
        public Set<UUID> getRelated(TeamType<?> type) {
            return Collections.emptySet();
        }
        public Map<ResourceLocation, Set<UUID>> getAllRelated() {
            return new HashMap<>();
        }
        public Set<UUID> getRelatedBy(TeamType<?> type) {
            return Collections.emptySet();
        }
        public void markDirty() {
        }
        public boolean isEmpty() {
            return true;
        }
    };

    /** Ids of GROUP teams of the given type this entity belongs to. Unmodifiable. */
    Set<UUID> getTeamIds(TeamType<?> type);

    /** Copy of all GROUP memberships keyed by type id. */
    Map<ResourceLocation, Set<UUID>> getAllTeamIds();

    /** Entity ids this entity is related to under the given RELATION type. Unmodifiable. */
    Set<UUID> getRelated(TeamType<?> type);

    /** Copy of all RELATION sets keyed by type id. */
    Map<ResourceLocation, Set<UUID>> getAllRelated();

    /** Entities that list this one under the given RELATION type. */
    Set<UUID> getRelatedBy(TeamType<?> type);

    void markDirty();

    /** Determine if this entity has no team data at all. */
    default boolean isEmpty() {
        return false;
    }
}
