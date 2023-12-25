package com.github.manasmods.manascore.storage;

import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.api.storage.StorageEvents.StoraceFactory;
import com.github.manasmods.manascore.api.storage.StorageEvents.StorageRegistry;
import com.github.manasmods.manascore.core.injection.StorageHolder;
import com.mojang.datafixers.util.Pair;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class StorageManager {
    private static final StorageRegistryImpl<Entity> ENTITY_STORAGE_REGISTRY = new StorageRegistryImpl<>();

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.invoker().register(ENTITY_STORAGE_REGISTRY);
    }

    public static void constructEntityStorage(Entity entity, CompoundTag storageTag) {
        ENTITY_STORAGE_REGISTRY.attach(entity, storageTag);
    }

    @ExpectPlatform
    public static void syncTracking(Entity source) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void syncTarget(Entity source, ServerPlayer target) {
        throw new AssertionError();
    }

    private static class StorageRegistryImpl<T extends StorageHolder> implements StorageRegistry<T> {
        private final Map<ResourceLocation, Pair<Predicate<T>, StoraceFactory<T>>> registry = new HashMap<>();

        @Override
        public void register(ResourceLocation id, Predicate<T> attachCheck, StoraceFactory<T> factory) {
            this.registry.put(id, Pair.of(attachCheck, factory));
        }

        public void attach(T target, CompoundTag storageTag) {
            this.registry.forEach((id, checkAndFactory) -> {
                if (!checkAndFactory.getFirst().test(target)) return;
                CompoundTag entryTag = new CompoundTag();
                checkAndFactory.getSecond().create(target);
            });
        }
    }
}
