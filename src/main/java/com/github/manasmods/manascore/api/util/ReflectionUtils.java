/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.util;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;

@Log4j2
public class ReflectionUtils {
    @Nullable
    public static <T> RegistryObject<T> getRegistryObjectFromField(AnnotationData annotationData, Field field, Class<T> type) {
        Objects.requireNonNull(type);
        try {
            field.setAccessible(true);
            //noinspection unchecked
            return (RegistryObject<T>) field.get(null); // no check due to catch below
        } catch (IllegalAccessException e) {
            log.error("Could not load data from field {} in class {}", field.getName(), annotationData.clazz().getClassName());
            log.throwing(e);
        } catch (ClassCastException e) {
            log.error("Could not cast field {} in class {} to RegistryObject<{}> type", field.getName(), annotationData.clazz().getClassName(), type.getName());
            log.throwing(e);
        }

        return null;
    }

    @Nullable
    public static <T> DeferredRegister<T> getDeferredRegisterFromField(AnnotationData annotationData, Field field, Class<T> type) {
        Objects.requireNonNull(type);
        try {
            field.setAccessible(true);
            //noinspection unchecked
            return (DeferredRegister<T>) field.get(null); // no check due to catch below
        } catch (IllegalAccessException e) {
            log.error("Could not load data from field {} in class {}", field.getName(), annotationData.clazz().getClassName());
            log.throwing(e);
        } catch (ClassCastException e) {
            log.error("Could not cast field {} in class {} to RegistryObject<{}> type", field.getName(), annotationData.clazz().getClassName(), type.getName());
            log.throwing(e);
        }
        return null;
    }
}
