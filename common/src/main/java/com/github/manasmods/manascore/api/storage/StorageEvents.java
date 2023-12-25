package com.github.manasmods.manascore.api.storage;

import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public interface StorageEvents {
    Event<RegisterStorage<Entity>> REGISTER_ENTITY_STORAGE = EventFactory.createLoop();

    @FunctionalInterface
    interface RegisterStorage<T extends StorageHolder> {
        void register(StorageRegistry<T> registry);
    }

    @FunctionalInterface
    interface StoraceFactory<T extends StorageHolder, S extends Storage> {
        S create(T target);
    }

    interface StorageRegistry<T extends StorageHolder> {
        <S extends Storage> StorageKey<S> register(ResourceLocation id, Class<S> storageClass, Predicate<T> attachCheck, StoraceFactory<T, S> factory);
    }
}
