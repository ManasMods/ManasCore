package com.github.manasmods.manascore.fabric;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.storage.fabric.StorageEventHandler;
import net.fabricmc.api.ModInitializer;

public class ManasCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCore.init();
        StorageEventHandler.init();
    }
}
