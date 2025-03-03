package io.github.manasmods.manascore.config.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for defining a config file in TOML format.
 * Extend this class to create custom mod configs.
 * <p>
 * Supports automatic loading, saving, and syncing of fields.
 * Reads {@link Comment} annotations to add comments in TOML files.
 */
public abstract class ManasConfig {
    @Getter
    private CommentedFileConfig config;

    /**
     * Define the file name for the config (without extension).
     */
    public abstract String getFileName();

    /**
     * Returns the path where the config file is stored.
     */
    public Path getConfigPath() {
        return Paths.get("config", getFileName() + ".toml");
    }

    /**
     * Loads the config file from disk or creates a new one if it doesn't exist.
     */
    public void load() {
        Path path = getConfigPath();
        File file = path.toFile();
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        config = CommentedFileConfig.builder(path).autoreload().sync().build();
        config.load();
        applyToFields();
        save();
    }

    /**
     * Saves the config values from the class fields into the file.
     */
    public void save() {
        saveFromFields();
        config.save();
    }

    /**
     * Loads config values from a TOML-formatted string.
     */
    public void loadFromString(String tomlData) {
        if (tomlData == null || tomlData.isEmpty()) return;
        TomlParser parser = new TomlParser();
        Config parsedConfig = parser.parse(new StringReader(tomlData));
        config.putAll(parsedConfig);
        applyToFields();
    }

    /**
     * Reads values from the TOML config and applies them to fields.
     */
    private void applyToFields() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = config.getOrElse(field.getName(), field.get(this));
                if (value instanceof Config configSub && field.get(this) instanceof ManasSubConfig sub) {
                    sub.applySubConfigFields(configSub);
                    field.set(this, sub);
                } else if (value != null) field.set(this, ManasConfig.getFieldValueConverted(field, value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves field values into the config file.
     */
    private void saveFromFields() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof ManasSubConfig sub) {
                    CommentedConfig subConfig = config.get(field.getName());
                    if (subConfig == null) subConfig = config.createSubConfig();
                    sub.saveSubConfigFields(sub, subConfig);
                    config.set(field.getName(), subConfig);
                } else {
                    if (field.getType() == ResourceLocation.class && value instanceof ResourceLocation rl) value = rl.toString();
                    if (value != null) config.set(field.getName(), value);
                }
                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) config.setComment(field.getName(), comment.value());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts field values to the correct type when reading from the config.
     */
    public static Object getFieldValueConverted(Field field, Object value) {
        if (field.getType() == float.class && value instanceof Double d) return d.floatValue();
        if (field.getType() == ResourceLocation.class && value instanceof String s) return ResourceLocation.tryParse(s);
        if (field.getType().isEnum() && value instanceof String s) return Enum.valueOf((Class<Enum>) field.getType(), s);
        return value;
    }
}


