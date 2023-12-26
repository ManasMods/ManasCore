package com.github.manasmods.manascore.network.toclient;

import net.minecraft.nbt.CompoundTag;

public interface StorageSyncPacket {
    boolean isUpdate();

    CompoundTag getStorageTag();
}
