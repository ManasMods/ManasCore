package com.github.manasmods.manascore;

import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.storage.StorageManager;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ManasCore {
    public static final String MOD_ID = "manascore";
    public static final Logger Logger = LogManager.getLogger("ManasCore");

    public static void init() {
        StorageManager.init();

        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {
            StorageKey<TestStorage> key = registry.register(new ResourceLocation(MOD_ID, "test"), TestStorage.class, entity -> true, TestStorage::new);

        });
    }

    private static class TestStorage extends Storage {

        TestStorage(Entity entity) {

        }

        @Override
        public void save(CompoundTag data) {

        }

        @Override
        public void load(CompoundTag data) {

        }
    }
}
