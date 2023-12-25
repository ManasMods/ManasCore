package com.github.manasmods.testmod;

import com.github.manasmods.testmod.storage.StorageTest;
import net.fabricmc.api.ModInitializer;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        StorageTest.init();
    }
}
