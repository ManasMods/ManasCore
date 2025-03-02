package io.github.manasmods.manascore.config;

import io.github.manasmods.manascore.config.api.ManasConfig;

import java.util.HashMap;
import java.util.Map;

public class ConfigRegistry {
    private static final Map<Class<? extends ManasConfig>, ManasConfig> CONFIGS = new HashMap<>();

    public static void loadAllConfigs() {
        for (ManasConfig config : CONFIGS.values()) {
            config.load();
        }
    }

    public static void saveAllConfigs() {
        for (ManasConfig config : CONFIGS.values()) {
            config.save();
        }
    }

    public static <T extends ManasConfig> T getConfig(Class<T> configClass) {
        return configClass.cast(CONFIGS.get(configClass));
    }

    public static void registerConfig(ManasConfig configInstance) {
        CONFIGS.put(configInstance.getClass(), configInstance);
    }
}
