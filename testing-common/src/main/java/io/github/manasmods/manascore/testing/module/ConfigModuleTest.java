package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.testing.configs.TestConfig;

public class ConfigModuleTest {

    public static void init() {
        System.out.println("ConfigModuleTest initialized");
        ConfigRegistry.registerConfig(new TestConfig());
        ConfigRegistry.createConfigs();

        TestConfig config = (TestConfig) ConfigRegistry.getConfig(TestConfig.class);
        System.out.println(config.test);

    }


}
