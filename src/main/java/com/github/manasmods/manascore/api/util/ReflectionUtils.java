/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.util;

import lombok.extern.log4j.Log4j2;
import net.minecraft.world.item.Item;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@Log4j2
public class ReflectionUtils {
    @Nullable
    public static RegistryObject<Item> getRegistryObjectFromField(AnnotationData annotationData, Field field) {
        try {
            field.setAccessible(true);
            //noinspection unchecked
            return (RegistryObject<Item>) field.get(null); // no check due to catch below
        } catch (IllegalAccessException e) {
            log.error("Could not load data from field {} in class {}", field.getName(), annotationData.clazz().getClassName());
            log.throwing(e);
        } catch (ClassCastException e) {
            log.error("Could not cast field {} in class {} to RegistryObject<Item> type", field.getName(), annotationData.clazz().getClassName());
            log.throwing(e);
        }

        return null;
    }
}
