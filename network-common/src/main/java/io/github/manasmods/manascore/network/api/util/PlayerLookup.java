package io.github.manasmods.manascore.network.api.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UtilityClass
public final class PlayerLookup {
    /**
     * Returns a collection of players tracking the entity.
     */
    public static Collection<ServerPlayer> tracking(@NonNull Entity entity) {
        ChunkSource manager = entity.level().getChunkSource();
        if (!(manager instanceof ServerChunkCache cache)) throw new IllegalArgumentException("Only supported on server worlds!");
        ChunkMap.TrackedEntity trackedEntity = cache.chunkMap.entityMap.get(entity.getId());
        if (trackedEntity == null) return new ArrayList<>();
        List<ServerPlayer> players = new ArrayList<>(trackedEntity.seenBy.size());
        for (ServerPlayerConnection connection : trackedEntity.seenBy) players.add(connection.getPlayer());
        return players;
    }

    /**
     * Returns a collection of players tracking the entity and the entity itself (only if the entity is a {@link ServerPlayer}).
     */
    public static Collection<ServerPlayer> trackingAndSelf(@NonNull Entity entity) {
        Collection<ServerPlayer> tracking = tracking(entity);
        if (!(entity instanceof ServerPlayer player)) return tracking;
        List<ServerPlayer> watchers = new ArrayList<>(tracking.size() + 1);
        watchers.add(player);
        watchers.addAll(tracking);
        return watchers;
    }

    public static Collection<ServerPlayer> tracking(@NonNull LevelChunk chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel level)) throw new IllegalArgumentException("Only supported on server worlds!");
        return level.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false);
    }
}
