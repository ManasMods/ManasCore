package com.github.manasmods.manascore.storage;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.core.injection.StorageHolder;
import com.github.manasmods.manascore.storage.StorageManager.StorageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CombinedStorage {
    private final Map<ResourceLocation, Storage> storages = new HashMap<>();
    private final StorageType type;
    private final StorageHolder holder;

    public CombinedStorage(StorageType type, StorageHolder holder) {
        this.type = type;
        this.holder = holder;
    }

    public CombinedStorage(StorageHolder holder, CompoundTag tag) {
        this(StorageType.valueOf(tag.getString("type")), holder);
        if (tag.contains("manascore_registry_storage")) {
            ListTag entriesTag = tag.getList("manascore_registry_storage", ListTag.TAG_COMPOUND);

            entriesTag.forEach(t -> {
                // Get serialized storage data
                CompoundTag entryTag = (CompoundTag) t;
                // Get storage id
                ResourceLocation id = new ResourceLocation(entryTag.getString("manascore_registry_storage_id"));
                // Construct storage
                Storage storage = StorageManager.constructCombinedStorage(this.type, id, holder);
                if (storage == null) {
                    ManasCore.Logger.warn("Failed to construct storage for id {}. All information about this storage will be dropped!", id);
                    return;
                }
                // Load storage data
                storage.load(entryTag);
                // Put storage into map
                this.storages.put(id, storage);
            });
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", this.type.name());

        ListTag entriesTag = new ListTag();
        this.storages.forEach((id, storage) -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("manascore_registry_storage_id", id.toString());
            storage.save(entryTag);
            entriesTag.add(entryTag);
        });

        tag.put("manascore_registry_storage", entriesTag);
        return tag;
    }

    public void add(ResourceLocation id, Storage storage) {
        this.storages.put(id, storage);
    }
}
