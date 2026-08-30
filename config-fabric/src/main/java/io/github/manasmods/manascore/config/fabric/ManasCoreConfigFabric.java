package io.github.manasmods.manascore.config.fabric;

import io.github.manasmods.manascore.config.ManasCoreConfig;
import net.fabricmc.api.ModInitializer;

public class ManasCoreConfigFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreConfig.init();
    }
}