package com.github.manasmods.manascore.api.config;

import com.github.manasmods.manascore.config.ConfigInstance;
import com.github.manasmods.manascore.config.ManasConfigManager;
import com.github.manasmods.manascore.exceptions.DuplicateConfigurationException;

import java.io.File;
import java.util.function.Supplier;

public class ManasConfig {
    public static <T> ConfigInstance<T> registerConfig(File file, Class<T> type, Supplier<T> defaultConfigFactory) throws DuplicateConfigurationException {
        return ManasConfigManager.registerConfig(file, type, defaultConfigFactory);
    }

}
