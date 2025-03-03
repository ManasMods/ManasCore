package io.github.manasmods.manascore.config.api;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;

public abstract class ManasSubConfig {
    public void applySubConfigFields(Config sourceConfig) {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = sourceConfig.get(field.getName());
                if (value instanceof Config configSub && field.get(this) instanceof ManasSubConfig sub) {
                    sub.applySubConfigFields(configSub);
                    field.set(this, sub);
                } else if (value != null) field.set(this, ManasConfig.getFieldValueConverted(field, value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveSubConfigFields(ManasSubConfig subConfigInstance, CommentedConfig config) {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(subConfigInstance);
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
}

