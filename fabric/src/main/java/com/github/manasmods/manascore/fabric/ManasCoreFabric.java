package com.github.manasmods.manascore.fabric;

import com.github.manasmods.manascore.ManasCore;
import net.fabricmc.api.ModInitializer;

public class ManasCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCore.init();
    }
}
