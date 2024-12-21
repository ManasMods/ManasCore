package io.github.manasmods.manascore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.manasmods.manascore.config.api.ManasConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ConfigRegistry {

    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final Set<ManasConfig> registeredConfigs = new HashSet<>();
    private static final Set<ManasConfig> loadedConfigs = new HashSet<>();

    public static void registerConfig(ManasConfig config) {
        registeredConfigs.add(config);
        ManasCoreConfig.LOG.info(config.getClass().getSimpleName() + " registered");
    }

    public static void loadConfigs() {
        registeredConfigs.forEach(config -> {
            Path startPath = Paths.get("config/" + config.getClass().getSimpleName().toLowerCase() + ".json");
            try {
                if (Files.exists(startPath)) {
                    String string = Files.readString(startPath);
                    ManasConfig loadedConfig = gson.fromJson(string, config.getClass());
                    loadedConfigs.add(loadedConfig);
                    ManasCoreConfig.LOG.info( config.getClass().getSimpleName() + " loaded");
                } else {
                    ManasCoreConfig.LOG.info( config.getClass().getSimpleName() + " not found");
                }
            } catch (IOException e) {
                ManasCoreConfig.LOG.error("Error loading config: " + config.getClass().getSimpleName());
                e.printStackTrace();
            }
        });
    }

    public static ManasConfig getConfig(Class<? extends ManasConfig> clazz) {
        return loadedConfigs.stream()
                .filter(config -> config.getClass().getSimpleName().equalsIgnoreCase(clazz.getSimpleName().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public static void createConfigs() {
        registeredConfigs.forEach(config -> {
            String content = gson.toJson(config);
            Path startPath = Paths.get("config/" + "/" + config.getClass().getSimpleName() + ".json");
            try {
                if (!Files.exists(startPath)) {
                    Files.createDirectories(startPath.getParent());
                    Files.write(startPath, content.getBytes());
                    ManasCoreConfig.LOG.info("Config: " + config.getClass().getSimpleName() + " created");
                } else {
                    ManasCoreConfig.LOG.info("Config: " + config.getClass().getSimpleName() + " already exists");
                }
            } catch (IOException e) {
                ManasCoreConfig.LOG.error("Error creating config: " + config.getClass().getSimpleName());
                e.printStackTrace();
            }
        });
        loadConfigs();
    }
}
