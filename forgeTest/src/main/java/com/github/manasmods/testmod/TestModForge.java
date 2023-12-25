package com.github.manasmods.testmod;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TestModForge.MOD_ID)
public class TestModForge {
    public static final String MOD_ID = "testmod";

    public TestModForge() {
        EventBuses.registerModEventBus(TestModForge.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        TestMod.init();
    }
}
