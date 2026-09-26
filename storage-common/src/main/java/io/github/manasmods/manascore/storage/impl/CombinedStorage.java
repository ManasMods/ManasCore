package io.github.manasmods.manascore.storage.impl;

import io.github.manasmods.manascore.storage.api.Storage;
import io.github.manasmods.manascore.storage.api.StorageHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static io.github.manasmods.manascore.storage.ManasCoreStorage.LOG;

public class CombinedStorage {
    private static final String STORAGE_LIST_KEY = "manascore_registry_storage";
    private static final String STORAGE_ID_KEY = "manascore_registry_storage_id";
    private final Map<ResourceLocation, Storage> storages = new HashMap<>();
    private final StorageHolder holder;

    public CombinedStorage(StorageHolder holder) {
        this.holder = holder;
    }

    public CompoundTag toNBT() {
        return this.toNBT(true);
    }

    public CompoundTag toNBT(boolean includeOwnerOnly) {
        CompoundTag tag = new CompoundTag();

        ListTag entriesTag = new ListTag();
        this.storages.forEach((id, storage) -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(STORAGE_ID_KEY, id.toString());
            if (includeOwnerOnly || !storage.isOwnerOnly()) storage.save(entryTag);
            entriesTag.add(entryTag);
        });

        tag.put(STORAGE_LIST_KEY, entriesTag);
        return tag;
    }

    public void load(CompoundTag tag) {
        ListTag entriesTag = tag.getList(STORAGE_LIST_KEY, Tag.TAG_COMPOUND);

        entriesTag.forEach(t -> {
            // Get serialized storage data
            CompoundTag entryTag = (CompoundTag) t;
            // Get storage id
            ResourceLocation id = ResourceLocation.parse(entryTag.getString(STORAGE_ID_KEY));
            // Construct storage
            Storage storage = StorageManager.constructStorageFor(this.holder.manasCore$getStorageType(), id, holder);
            if (storage == null) {
                LOG.warn("Failed to construct storage for id {}. All information about this storage will be dropped!", id);
                return;
            }
            // Load storage data
            storage.load(entryTag);
            // Put storage into map
            this.storages.put(id, storage);
        });
    }

    public void handleUpdatePacket(CompoundTag tag) {
        ListTag entriesTag = tag.getList(STORAGE_LIST_KEY, Tag.TAG_COMPOUND);

        for (Tag e : entriesTag) {
            CompoundTag entryTag = (CompoundTag) e;
            ResourceLocation id = ResourceLocation.tryParse(entryTag.getString(STORAGE_ID_KEY));
            Storage storage = this.storages.get(id);
            if (storage == null) {
                LOG.warn("Failed to find storage for id {}. All information about this storage will be dropped!", id);
                continue;
            }

            storage.loadUpdate(entryTag);
        }
    }

    public void add(ResourceLocation id, Storage storage) {
        this.storages.put(id, storage);
    }

    @Nullable
    public Storage get(ResourceLocation id) {
        return this.storages.get(id);
    }

    public CompoundTag createUpdatePacket(boolean clean) {
        return this.createUpdatePacket(clean, true);
    }

    public CompoundTag createUpdatePacket(boolean clean, boolean includeOwnerOnly) {
        CompoundTag tag = new CompoundTag();

        ListTag entriesTag = new ListTag();
        this.storages.forEach((id, storage) -> {
            if (!storage.isDirty()) return;
            if (!includeOwnerOnly && storage.isOwnerOnly()) {
                if (clean) storage.clearDirty();
                return;
            }

            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(STORAGE_ID_KEY, id.toString());
            storage.saveOutdated(entryTag);
            entriesTag.add(entryTag);
            if (clean) storage.clearDirty();
        });

        tag.put(STORAGE_LIST_KEY, entriesTag);
        return tag;
    }

    /**
     * Copy of an update packet created by {@link #createUpdatePacket(boolean, boolean)} without the owner-only entries.
     */
    public CompoundTag stripOwnerOnly(CompoundTag updatePacket) {
        ListTag entriesTag = new ListTag();
        for (Tag e : updatePacket.getList(STORAGE_LIST_KEY, Tag.TAG_COMPOUND)) {
            Storage storage = this.storages.get(ResourceLocation.tryParse(((CompoundTag) e).getString(STORAGE_ID_KEY)));
            if (storage == null || !storage.isOwnerOnly()) entriesTag.add(e);
        }

        CompoundTag tag = new CompoundTag();
        tag.put(STORAGE_LIST_KEY, entriesTag);
        return tag;
    }

    public static boolean hasEntries(CompoundTag packet) {
        return !packet.getList(STORAGE_LIST_KEY, Tag.TAG_COMPOUND).isEmpty();
    }

    public boolean isDirty() {
        for (Storage storage : this.storages.values()) {
            if (storage.isDirty()) return true;
        }
        return false;
    }
}
