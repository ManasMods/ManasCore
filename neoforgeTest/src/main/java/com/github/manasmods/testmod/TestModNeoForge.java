package com.github.manasmods.testmod;

import net.neoforged.fml.common.Mod;

@Mod(TestModNeoForge.MOD_ID)
public class TestModNeoForge {
    public static final String MOD_ID = "testmod";

    public TestModNeoForge() {
        TestMod.init();
    }
}
