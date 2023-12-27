package com.github.manasmods.manascore;

import com.github.manasmods.manascore.client.ManasCoreClient;
import com.github.manasmods.manascore.network.NetworkManager;
import com.github.manasmods.manascore.storage.StorageManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ManasCore {
    public static final String MOD_ID = "manascore";
    public static final Logger Logger = LogManager.getLogger("ManasCore");

    public static void init() {
        LifecycleEvent.SETUP.register(StorageManager::init);
        NetworkManager.init();
        if (Platform.getEnvironment() == Env.CLIENT) {
            ManasCoreClient.init();
        }
    }
}
