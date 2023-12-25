package com.github.manasmods.manascore.api.storage;

import net.minecraft.nbt.CompoundTag;

public abstract class Storage {
    private boolean dirty = false;

    /**
     * Used to save data to the entity.
     * Add all information to the given tag.
     */
    public abstract void save(CompoundTag data);

    /**
     * Used to load data from the entity.
     * Read all information from the given tag.
     */
    public abstract void load(CompoundTag data);

    /**
     * Used to mark the storage as dirty.
     * This will cause the storage to be synchronized.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Used to check if the storage is dirty.
     */
    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * Used to clear the dirty flag.
     */
    public void clearDirty() {
        this.dirty = false;
    }
}
