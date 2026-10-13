package io.github.manasmods.manascore.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlWriter;
import io.github.manasmods.manascore.config.api.ConfigReloadResult;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.api.ManasSubConfig;
import io.github.manasmods.manascore.config.api.SyncToClient;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        ConfigReloadResult result = reloadConfigs();
        if (result.isSuccess()) return;
        if (result.getCause() instanceof RuntimeException exception) throw exception;
        throw new RuntimeException(result.getCause());
    }

    public static ConfigReloadResult reloadConfigs() {
        return reload(new ArrayList<>(CONFIGS.values()));
    }

    public static ConfigReloadResult reloadConfigs(String modId) {
        List<ManasConfig> configs = new ArrayList<>();
        for (ManasConfig config : CONFIGS.values()) {
            if (config.getModId().equals(modId)) configs.add(config);
        }
        return reload(configs);
    }

    private static ConfigReloadResult reload(List<ManasConfig> configs) {
        Map<ManasConfig, CommentedFileConfig> parsed = new HashMap<>();
        for (ManasConfig config : configs) {
            try {
                parsed.put(config, config.read());
            } catch (Exception e) {
                return new ConfigReloadResult(config.getConfigPath(), e);
            }
        }

        List<FieldValue> previous = new ArrayList<>();
        for (ManasConfig config : configs) collect(config, previous);
        for (ManasConfig config : configs) {
            try {
                config.applyToFields(parsed.get(config));
            } catch (Exception e) {
                for (FieldValue value : previous) value.restore();
                return new ConfigReloadResult(config.getConfigPath(), e);
            }
        }

        List<Path> files = new ArrayList<>();
        for (ManasConfig config : configs) {
            config.commit(parsed.get(config));
            files.add(config.getConfigPath());
        }
        return new ConfigReloadResult(files);
    }

    private static void collect(Object holder, List<FieldValue> values) {
        for (Field field : holder.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(holder);
                values.add(new FieldValue(holder, field, value));
                if (value instanceof ManasSubConfig sub) collect(sub, values);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to read configuration field: " + field.getName(), e);
            }
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

    private static class FieldValue {
        private final Object holder;
        private final Field field;
        @Nullable
        private final Object value;

        private FieldValue(Object holder, Field field, @Nullable Object value) {
            this.holder = holder;
            this.field = field;
            this.value = value;
        }

        private void restore() {
            try {
                if (!Objects.equals(this.field.get(this.holder), this.value)) this.field.set(this.holder, this.value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to restore configuration field: " + this.field.getName(), e);
            }
        }
    }
}
