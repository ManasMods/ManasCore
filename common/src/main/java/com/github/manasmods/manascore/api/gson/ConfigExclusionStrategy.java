package com.github.manasmods.manascore.api.gson;

import com.github.manasmods.manascore.api.config.Excluded;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ConfigExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(Excluded.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
