package com.github.manasmods.forge.testmod;

import com.github.manasmods.testmod.TestMod;
import com.github.manasmods.testmod.gametest.SkillTests;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TestMod.MOD_ID)
public class TestModForge {

    public TestModForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(TestMod.MOD_ID, modEventBus);
        TestMod.init();

        modEventBus.addListener(this::registerTests);
    }

    private void registerTests(RegisterGameTestsEvent e) {
        e.register(SkillTests.class);
    }
}
