package io.github.manasmods.manascore.config;

import com.electronwill.nightconfig.toml.TomlWriter;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.api.SyncToClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the registration and management of all {@link ManasConfig} instances.
 */
public class ConfigRegistry {
    private static final Map<Class<? extends ManasConfig>, ManasConfig> CONFIGS = new ConcurrentHashMap<>();

    /**
     * Retrieves a registered config instance by class type.
     * <p>
     * @param  configClass The config class.
     * @return The instance of the requested config, or null if not registered.
     */
    public static <T extends ManasConfig> T getConfig(Class<T> configClass) {
        return configClass.cast(CONFIGS.get(configClass));
    }

    /**
     * Registers a new configuration and loads it.
     * <p>
     * @param configInstance The config instance to register.
     */
    public static void registerConfig(ManasConfig configInstance) {
        CONFIGS.put(configInstance.getClass(), configInstance);
        configInstance.load();
    }

    /**
     * Reloads all registered configurations from disk.
     */
    public static void loadConfigSyncData() {
        for (ManasConfig config : CONFIGS.values()) {
            config.load();
        }
    }

    /**
     * Saves all registered configurations to disk.
     */
    public static void saveAllConfigs() {
        for (ManasConfig config : CONFIGS.values()) {
            config.save();
        }
    }

    /**
     * Loads config data from a synced client-server map.
     * Only applies to configs annotated with {@link SyncToClient}.
     * <p>
     * @param map The config data received from the server.
     */
    public static void loadConfigSyncData(Map<String, String> map) {
        if (map == null || map.isEmpty()) return;
        CONFIGS.forEach((clazz, config) -> {
            if (!map.containsKey(clazz.getSimpleName())) return;
            if (!clazz.isAnnotationPresent(SyncToClient.class)) return;
            config.loadFromString(map.get(clazz.getSimpleName()));
        });
    }

    /**
     * Serializes all syncable configs into a map for server-to-client transmission.
     * <p>
     * @return A map containing serialized TOML configs.
     */
    public static Map<String, String> getConfigSyncData() {
        Map<String, String> configData = new HashMap<>();
        CONFIGS.forEach((clazz, config) -> {
            if (!clazz.isAnnotationPresent(SyncToClient.class)) return;
            config.load();
            configData.put(clazz.getSimpleName(), (new TomlWriter()).writeToString(config.getConfig()));
        });
        return configData;
    }

    /**
     * Serializes a specific config for syncing.
     * <p>
     * @param  configClass The class of the config to sync.
     * @return A map containing the serialized TOML data for the requested config.
     */
    public static Map<String, String> getConfigSyncData(Class<? extends ManasConfig> configClass) {
        Map<String, String> configData = new HashMap<>();
        if (!CONFIGS.containsKey(configClass)) return configData;
        if (!configClass.isAnnotationPresent(SyncToClient.class)) return configData;

        ManasConfig config = CONFIGS.get(configClass);
        config.load();
        configData.put(configClass.getSimpleName(), (new TomlWriter()).writeToString(config.getConfig()));
        return configData;
    }
}
