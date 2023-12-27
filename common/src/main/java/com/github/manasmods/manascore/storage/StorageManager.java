package com.github.manasmods.manascore.storage;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.api.storage.StorageEvents.StoraceFactory;
import com.github.manasmods.manascore.api.storage.StorageEvents.StorageRegistry;
import com.github.manasmods.manascore.api.storage.StorageHolder;
import com.github.manasmods.manascore.api.storage.StorageType;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.network.toclient.StorageSyncPacket;
import com.github.manasmods.manascore.network.toclient.SyncChunkStoragePacket;
import com.github.manasmods.manascore.network.toclient.SyncEntityStoragePacket;
import com.github.manasmods.manascore.network.toclient.SyncWorldStoragePacket;
import com.mojang.datafixers.util.Pair;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class StorageManager {
    private static final StorageRegistryImpl<Entity> ENTITY_STORAGE_REGISTRY = new StorageRegistryImpl<>();
    private static final StorageRegistryImpl<LevelChunk> CHUNK_STORAGE_REGISTRY = new StorageRegistryImpl<>();
    private static final StorageRegistryImpl<Level> LEVEL_STORAGE_REGISTRY = new StorageRegistryImpl<>();

    public static void init() {
        StorageEvents.REGISTER_WORLD_STORAGE.invoker().register(LEVEL_STORAGE_REGISTRY);
        StorageEvents.REGISTER_CHUNK_STORAGE.invoker().register(CHUNK_STORAGE_REGISTRY);
        StorageEvents.REGISTER_ENTITY_STORAGE.invoker().register(ENTITY_STORAGE_REGISTRY);
        // Initial client syncronization
        PlayerEvent.PLAYER_JOIN.register(player -> {
            player.manasCore$sync(player);
            ServerLevel level = player.serverLevel();
            level.getChunkAt(player.blockPosition()).manasCore$sync(player);
            level.manasCore$sync(player);
        });
        // Copy storage from old player to new player
        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) -> newPlayer.manasCore$setCombinedStorage(oldPlayer.manasCore$getCombinedStorage()));
    }

    public static void initialStorageFilling(StorageHolder holder) {
        switch (holder.manasCore$getStorageType()) {
            case ENTITY -> ENTITY_STORAGE_REGISTRY.attach((Entity) holder);
            case CHUNK -> CHUNK_STORAGE_REGISTRY.attach((LevelChunk) holder);
            case WORLD -> LEVEL_STORAGE_REGISTRY.attach((Level) holder);
        }
    }

    public static void syncTracking(StorageHolder source) {
        syncTracking(source, false);
    }

    public static void syncTracking(StorageHolder source, boolean update) {
        NetworkManager.CHANNEL.sendToPlayers(source.manasCore$getTrackingPlayers(), createSyncPacket(source, update));
    }

    public static void syncTarget(StorageHolder source, ServerPlayer target) {
        NetworkManager.CHANNEL.sendToPlayer(target, createSyncPacket(source, false));
    }

    private static StorageSyncPacket createSyncPacket(StorageHolder source, boolean update) {
        return switch (source.manasCore$getStorageType()) {
            case ENTITY -> {
                Entity sourceEntity = (Entity) source;
                yield new SyncEntityStoragePacket(
                        update,
                        sourceEntity.getId(),
                        update ? sourceEntity.manasCore$getCombinedStorage().createUpdatePacket(true)
                                : sourceEntity.manasCore$getCombinedStorage().toNBT()
                );
            }
            case CHUNK -> {
                LevelChunk sourceChunk = (LevelChunk) source;
                yield new SyncChunkStoragePacket(
                        update,
                        sourceChunk.getPos(),
                        update ? sourceChunk.manasCore$getCombinedStorage().createUpdatePacket(true)
                                : sourceChunk.manasCore$getCombinedStorage().toNBT()
                );
            }
            case WORLD -> new SyncWorldStoragePacket(
                    update,
                    update ? source.manasCore$getCombinedStorage().createUpdatePacket(true)
                            : source.manasCore$getCombinedStorage().toNBT()
            );
        };
    }

    @Nullable
    public static Storage constructStorageFor(StorageType type, ResourceLocation id, StorageHolder holder) {
        return switch (type) {
            case ENTITY -> ENTITY_STORAGE_REGISTRY.registry.get(id).getSecond().create((Entity) holder);
            case CHUNK -> CHUNK_STORAGE_REGISTRY.registry.get(id).getSecond().create((LevelChunk) holder);
            case WORLD -> LEVEL_STORAGE_REGISTRY.registry.get(id).getSecond().create((Level) holder);
        };
    }

    @Nullable
    public static <T extends Storage> T getStorage(StorageHolder holder, StorageKey<T> storageKey) {
        return holder.manasCore$getStorage(storageKey);
    }

    private static class StorageRegistryImpl<T extends StorageHolder> implements StorageRegistry<T> {
        private final Map<ResourceLocation, Pair<Predicate<T>, StoraceFactory<T, ?>>> registry = new HashMap<>();

        @Override
        public <S extends Storage> StorageKey<S> register(ResourceLocation id, Class<S> storageClass, Predicate<T> attachCheck, StoraceFactory<T, S> factory) {
            this.registry.put(id, Pair.of(attachCheck, factory));
            return new StorageKey<>(id, storageClass);
        }

        public void attach(T target) {
            this.registry.forEach((id, checkAndFactory) -> {
                if (!checkAndFactory.getFirst().test(target)) return;
                Storage storage = checkAndFactory.getSecond().create(target);
                target.manasCore$attachStorage(id, storage);
            });
        }
    }

    public record StorageKey<T extends Storage>(ResourceLocation id, Class<T> type) {
    }
}
