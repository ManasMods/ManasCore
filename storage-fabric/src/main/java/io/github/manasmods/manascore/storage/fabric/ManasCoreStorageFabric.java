/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.storage.fabric;

import io.github.manasmods.manascore.storage.ManasCoreStorage;
import io.github.manasmods.manascore.storage.impl.StorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public class ManasCoreStorageFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreStorage.init();
        EntityTrackingEvents.START_TRACKING.register((entity, serverPlayer) -> StorageManager.syncTarget(entity, serverPlayer));
    }
}