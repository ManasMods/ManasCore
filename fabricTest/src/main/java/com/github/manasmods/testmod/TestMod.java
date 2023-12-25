package com.github.manasmods.testmod;

import com.github.manasmods.manascore.ManasCore;
import net.fabricmc.api.ModInitializer;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCore.Logger.info("TestMod initialized!");
    }
}
