package com.github.manasmods.manascore.api.storage;

import com.github.manasmods.manascore.core.injection.StorageHolder;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public interface StorageEvents {
    Event<RegisterStorage<Entity>> REGISTER_ENTITY_STORAGE = EventFactory.createEventResult();

    @FunctionalInterface
    interface RegisterStorage<T extends StorageHolder> {
        void register(StorageRegistry<T> registry);
    }

    @FunctionalInterface
    interface StoraceFactory<T extends StorageHolder> {
        Storage create(T target);
    }

    interface StorageRegistry<T extends StorageHolder> {
        void register(ResourceLocation id, Predicate<T> attachCheck, StoraceFactory<T> factory);
    }
}
