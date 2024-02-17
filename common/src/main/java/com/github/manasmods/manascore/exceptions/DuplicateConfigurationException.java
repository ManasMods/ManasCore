package com.github.manasmods.manascore.exceptions;

import java.io.File;

public class DuplicateConfigurationException extends IllegalStateException {
    public DuplicateConfigurationException(File configFile) {
        super("Config file " + configFile + " is already registered.");
    }
}
