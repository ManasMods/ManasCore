package com.github.manasmods.testmod;

import com.github.manasmods.manascore.api.registry.Register;
import com.github.manasmods.testmod.client.TestModClient;
import com.github.manasmods.testmod.registry.RegisterTest;
import com.github.manasmods.testmod.storage.StorageTest;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public class TestMod {
    public static final String MOD_ID = "testmod";
    public  static final Register REGISTER = new Register("testmod");

    public static void init() {
        REGISTER.init(RegisterTest::init);
        StorageTest.init();
        RegisterTest.init();

        if(Platform.getEnvironment() == Env.CLIENT) {
            TestModClient.init();
        }
    }
}
