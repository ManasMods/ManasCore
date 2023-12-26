package com.github.manasmods.testmod;

import com.github.manasmods.testmod.storage.StorageTest;

public class TestMod {
    public static final String MOD_ID = "testmod";

    public static void init() {
        StorageTest.init();
    }
}
