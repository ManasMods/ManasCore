package com.github.manasmods.manascore.storage.fabric;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class StorageManagerImpl {
    public static void syncTracking(Entity entity) {
        getTrackingEntitiesOf(entity).forEach(player -> syncTarget(entity, player));
    }

    private static Iterable<ServerPlayer> getTrackingEntitiesOf(Entity entity) {
        Level level = entity.level();
        if (level != null && !level.isClientSide()) {
            // Get all players tracking this entity
            Deque<ServerPlayer> watchers = new ArrayDeque<>();
            // Add self when it's a player
            if (entity instanceof ServerPlayer player && player.connection != null) watchers.addFirst(player);
            return watchers;
        }

        return List.of();
    }

    public static void syncTarget(Entity source, ServerPlayer target) {
        //TODO create & send packet
    }
}
