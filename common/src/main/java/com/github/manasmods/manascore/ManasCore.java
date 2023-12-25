package com.github.manasmods.manascore;

import com.github.manasmods.manascore.storage.StorageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ManasCore {
    public static final String MOD_ID = "manascore";
    public static final Logger Logger = LogManager.getLogger("ManasCore");

    public static void init() {
        StorageManager.init();
    }
}
