package com.github.manasmods.manascore.storage.fabric;

import com.github.manasmods.manascore.storage.StorageManager;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public class StorageEventHandler {
    public static void init() {
        EntityTrackingEvents.START_TRACKING.register(StorageManager::syncTarget);
    }
}
