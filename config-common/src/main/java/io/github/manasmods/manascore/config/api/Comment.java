/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.config.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to add comments to fields inside a {@link ManasConfig}.
 * These comments will be included in the generated TOML config files.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
    String value(); // The comment text to be added to the config file.
}