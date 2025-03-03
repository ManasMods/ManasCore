package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.testing.configs.SkillConfig;
import io.github.manasmods.manascore.testing.configs.TestConfig;

public class ConfigModuleTest {

    public static void init() {
        System.out.println("ConfigModuleTest initialized");
        ConfigRegistry.registerConfig(new SkillConfig());
        ConfigRegistry.registerConfig(new TestConfig());
        TestConfig testConfig = ConfigRegistry.getConfig(TestConfig.class);
        System.out.println(testConfig.test_subConfig.initialMessage);
    }
}
