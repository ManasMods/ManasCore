/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.config.api;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigReloadResult {
    private final List<Path> reloadedFiles;
    @Nullable
    private final Path failedFile;
    @Nullable
    private final Exception cause;

    public ConfigReloadResult(List<Path> reloadedFiles) {
        this.reloadedFiles = reloadedFiles;
        this.failedFile = null;
        this.cause = null;
    }

    public ConfigReloadResult(Path failedFile, Exception cause) {
        this.reloadedFiles = new ArrayList<>();
        this.failedFile = failedFile;
        this.cause = cause;
    }

    public boolean isSuccess() {
        return this.cause == null;
    }
}
