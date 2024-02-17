package com.github.manasmods.manascore.config;

import com.github.manasmods.manascore.api.gson.ColorTypeAdapter;
import com.github.manasmods.manascore.api.gson.ConfigExclusionStrategy;
import com.github.manasmods.manascore.api.gson.RegistrySupplierTypeAdapter;
import com.github.manasmods.manascore.api.gson.StyleTypeAdapter;
import com.github.manasmods.manascore.exceptions.DuplicateConfigurationException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ManasConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setExclusionStrategies(new ConfigExclusionStrategy())
            .registerTypeHierarchyAdapter(Component.class, new Component.SerializerAdapter())
            .registerTypeHierarchyAdapter(Style.class, new StyleTypeAdapter())
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(RegistrySupplier.class, new RegistrySupplierTypeAdapter<>())
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final ConcurrentHashMap<File, ConfigInstance<?>> registeredConfigs = new ConcurrentHashMap<>();

    public static <T> ConfigInstance<T> registerConfig(File file, Class<T> type, Supplier<T> defaultConfigFactory) throws DuplicateConfigurationException {
        // Prevent duplicate registrations
        if (registeredConfigs.containsKey(file)) throw new DuplicateConfigurationException(file);
        // Create a new config instance
        ConfigInstance<T> configInstance = new ConfigInstance<>(file, defaultConfigFactory, type);
        // Register the config instance
        registeredConfigs.put(file, configInstance);

        return configInstance;
    }

    @Nullable
    static <T> T loadConfig(File file, Class<T> type) {
        if (!file.exists()) return null;

        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            return GSON.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> void writeConfig(@NonNull File configFile, @NonNull T config) {
        try (var writer = new FileWriter(configFile, StandardCharsets.UTF_8, false)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
