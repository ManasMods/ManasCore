package com.github.manasmods.manascore.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ConfigInstance<T> {
    @Getter
    private final File configFile;
    private final Supplier<T> defaultConfigFactory;
    private final Class<T> type;
    @Getter
    private boolean initialized = false;
    private T config;
    private final List<BiConsumer<ConfigInstance<T>, T>> initializerListeners = new ArrayList<>();


    public T getConfig() {
        if (!initialized) {
            // Load the config
            this.config = ManasConfigManager.loadConfig(configFile, type);
            // If the config is null, create a new instance
            if (this.config == null) {
                this.config = defaultConfigFactory.get();
                ManasConfigManager.writeConfig(configFile, this.config);
            }
            initialized = true;
            // Notify listeners
            initializerListeners.forEach(listener -> listener.accept(this, config));
            initializerListeners.clear();
        }

        return config;
    }

    public void save() {
        ManasConfigManager.writeConfig(configFile, config);
    }
}
