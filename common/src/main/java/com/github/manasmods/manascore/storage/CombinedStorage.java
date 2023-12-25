package com.github.manasmods.manascore.storage;

import com.github.manasmods.manascore.api.storage.Storage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CombinedStorage {
    private final Map<ResourceLocation, Storage> storages = new HashMap<>();

    public CombinedStorage() {
    }

    public CombinedStorage(CompoundTag tag) {
        this();

    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

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


}
