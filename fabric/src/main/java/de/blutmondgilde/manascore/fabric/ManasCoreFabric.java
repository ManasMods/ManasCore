package de.blutmondgilde.manascore.fabric;

import de.blutmondgilde.manascore.ManasCore;
import net.fabricmc.api.ModInitializer;

public class ManasCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCore.init();
    }
}