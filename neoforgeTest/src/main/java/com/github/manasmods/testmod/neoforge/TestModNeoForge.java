package com.github.manasmods.testmod.neoforge;

import com.github.manasmods.testmod.TestMod;
import com.github.manasmods.testmod.gametest.SkillTests;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod(TestMod.MOD_ID)
public class TestModNeoForge {

    public TestModNeoForge(IEventBus modEventBus) {
        TestMod.init();
        modEventBus.addListener(this::registerTests);
    }

    private void registerTests(RegisterGameTestsEvent e) {
        e.register(SkillTests.class);
    }
}
