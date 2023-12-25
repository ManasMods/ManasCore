package com.github.manasmods.testmod.storage;

import com.github.manasmods.manascore.api.storage.StorageEvents;

public class StorageTest {
    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {

        });
    }
}
