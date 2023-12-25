package com.github.manasmods.manascore.storage.fabric;

import com.github.manasmods.manascore.network.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class StorageManagerImpl {
    public static void syncTracking(Entity entity, boolean update) {
        final FriendlyByteBuf buf = createPacketBuffer(entity);
        getTrackingEntitiesOf(entity).forEach(player -> syncTarget(player, buf));
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
        syncTarget(target, createPacketBuffer(source));
    }

    private static FriendlyByteBuf createPacketBuffer(final Entity source) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeEnum(source.manasCore$getStorageType());
        buf.writeInt(source.getId());
        buf.writeNbt(source.manasCore$getStorage());
        return buf;
    }

    private static void syncTarget(final ServerPlayer target, final FriendlyByteBuf buf) {
        ServerPlayNetworking.send(target, NetworkManager.CHANNEL, buf);
    }
}
