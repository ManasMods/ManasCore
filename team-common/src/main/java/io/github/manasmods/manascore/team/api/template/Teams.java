/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU Lesser General Public License 3
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
 * Entity side view of team membership.
 * It is implemented by the team storage and, on the server, by a read-only view over the saved table for ids that are not loaded.
 * <p>
 * Reads are safe on both sides. The storage is synced to its holder only.
 */
public interface Teams {
    /**
     * No-op fallback used by {@link TeamAPI#getTeamsFrom} when an entity has no team storage.
     * Reads return empty results and writes are ignored.
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

    /**
     * Used to get the ids of the {@code GROUP} teams of the given type this entity belongs to.
     * The result is unmodifiable.
     */
    Set<UUID> getTeamIds(TeamType<?> type);

    /**
     * Used to get a copy of all {@code GROUP} memberships keyed by type id.
     */
    Map<ResourceLocation, Set<UUID>> getAllTeamIds();

    /**
     * Used to get the ids of the entities this entity is related to under the given {@code RELATION} type.
     * The result is unmodifiable.
     */
    Set<UUID> getRelated(TeamType<?> type);

    /**
     * Used to get a copy of all {@code RELATION} sets keyed by type id.
     */
    Map<ResourceLocation, Set<UUID>> getAllRelated();

    /**
     * Used to get the ids of the entities that list this one under the given {@code RELATION} type.
     */
    Set<UUID> getRelatedBy(TeamType<?> type);

    /**
     * Used to mark the underlying storage as dirty.
     * Views that are not backed by a storage ignore it.
     */
    void markDirty();

    /**
     * Determine if this entity has no team data at all.
     */
    default boolean isEmpty() {
        return false;
    }
}
