/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.storage;

import io.github.manasmods.manascore.storage.impl.StorageManager;
import io.github.manasmods.manascore.storage.impl.network.ManasStorageNetwork;
import dev.architectury.event.events.common.LifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManasCoreStorage {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Storage");

    public static void init() {
        LOG.info("init");
        ManasStorageNetwork.init();
        LifecycleEvent.SETUP.register(StorageManager::init);
    }
}
