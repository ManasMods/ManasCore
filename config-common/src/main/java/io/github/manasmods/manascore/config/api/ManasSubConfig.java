package io.github.manasmods.manascore.config.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ManasSubConfig {
    public static void applyToFields(Object instance, CommentedConfig config) {
        Field[] fields = instance.getClass().getDeclaredFields();
        Arrays.sort(fields, Comparator.comparingInt(field -> field.getDeclaredAnnotations().length));
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = config.get(field.getName());
                if (value instanceof ManasSubConfig) {
                    Object subValue = field.get(instance);
                    if (subValue != null) {
                        CommentedConfig subConfig = config.get(field.getName());
                        if (subConfig == null) {
                            subConfig = config.createSubConfig();
                            config.set(field.getName(), subConfig);
                        }
                        ManasSubConfig.applyToFields(subValue, subConfig);
                    }
                    continue;
                }

                if (value != null) field.set(instance, ManasConfig.getFieldValueConverted(field, value));
                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) config.setComment(field.getName(), comment.value());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveFromFields(Object instance, CommentedConfig config) {
        Map<String, Object> orderedValues = new LinkedHashMap<>();
        Field[] fields = instance.getClass().getDeclaredFields();
        Arrays.sort(fields, Comparator.comparingInt(field -> field.getDeclaredAnnotations().length));

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value instanceof ManasSubConfig) {
                    CommentedConfig subConfig = config.get(field.getName());
                    if (subConfig == null) {
                        subConfig = config.createSubConfig();
                        config.set(field.getName(), subConfig);
                    }
                    ManasSubConfig.saveFromFields(value, subConfig);
                    continue;
                }

                if (field.getType() == ResourceLocation.class && value instanceof ResourceLocation rl) value = rl.toString();
                if (value != null) orderedValues.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<String, Object> entry : orderedValues.entrySet())
            config.set(entry.getKey(), entry.getValue());
    }
}

