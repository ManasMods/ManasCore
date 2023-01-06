/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import com.github.manasmods.manascore.api.data.gen.annotation.GenerateEntityLoot;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateEntityLoot.EmptyLootTable;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateEntityLoot.WithLootTables;
import com.github.manasmods.manascore.api.util.ReflectionUtils;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.manasmods.manascore.api.util.StreamUtils.distinctBy;

@Log4j2
public abstract class EntityLoot extends VanillaEntityLoot {
    private static final Type GEN_ANNOTATION = Type.getType(GenerateEntityLoot.class);

    @NonExtendable
    @Override
    public final void generate() {
        loadTables();
        final List<AnnotationData> annotations = new ArrayList<>();
        ModList.get().forEachModFile(modFile -> {
            modFile.getScanResult().getAnnotations()
                .stream()
                .filter(annotationData -> GEN_ANNOTATION.equals(annotationData.annotationType()))
                .forEach(annotations::add);
        });
        generateAnnotationLootTables(annotations);
    }

    @NonExtendable
    private void generateAnnotationLootTables(List<AnnotationData> annotations) {
        annotations.forEach(annotationData -> {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(annotationData.clazz().getClassName());
            } catch (ClassNotFoundException e) {
                log.error("Could not load class " + annotationData.clazz().getClassName());
                log.throwing(e);
            }
            if (clazz == null) return;

            List<Field> entityRegistryObjectFieldList = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType().equals(RegistryObject.class))
                .filter(field -> {
                    ParameterizedType registryObjectType = null;
                    try {
                        registryObjectType = ((ParameterizedType) field.getGenericType());
                    } catch (ClassCastException e) {
                        log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName());
                        log.throwing(e);
                    }
                    if (registryObjectType == null) return false;

                    Class<?> registryObjectClass = null;
                    try {
                        registryObjectClass = (Class<?>) registryObjectType.getActualTypeArguments()[0];
                    } catch (ClassCastException e) {
                        log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName() + ".");
                        log.throwing(e);
                    }
                    if (registryObjectClass == null) return false;

                    return EntityType.class.isAssignableFrom(registryObjectClass);
                })
                .toList();
            // TODO add Annotation Generators
            generateEmptyLootTables(annotationData, entityRegistryObjectFieldList);
        });
    }

    @NonExtendable
    private void generateEmptyLootTables(AnnotationData annotationData, List<Field> entityRegistryObjectFieldList) {
        for (Field registryObjectField : entityRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(EmptyLootTable.class)) continue;
            @SuppressWarnings("unchecked") RegistryObject<EntityType<?>> registryObject =
                (RegistryObject<EntityType<?>>) (Object) ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, EntityType.class);
            if (registryObject == null) continue;
            log.debug("Generating block loot for registry object {}", registryObject.getId());
            add(registryObject.get(), LootTable.lootTable());
        }
    }

    protected abstract void loadTables();

    @SuppressWarnings("unchecked")
    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return (Stream<EntityType<?>>) (Object) ModList.get().getAllScanData()
            .stream()
            .flatMap(modFileScanData -> modFileScanData.getAnnotations().stream())
            .filter(annotationData -> GEN_ANNOTATION.equals(annotationData.annotationType()))
            .flatMap(annotationData -> {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(annotationData.clazz().getClassName());
                } catch (ClassNotFoundException e) {
                    log.error("Could not load class " + annotationData.clazz().getClassName());
                    log.throwing(e);
                }
                if (clazz == null) return Stream.of();

                return Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> Modifier.isStatic(field.getModifiers()))
                    .filter(field -> field.getType().equals(DeferredRegister.class))
                    .filter(field -> {
                        ParameterizedType registryObjectType = null;
                        try {
                            registryObjectType = ((ParameterizedType) field.getGenericType());
                        } catch (ClassCastException e) {
                            log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName());
                            log.throwing(e);
                        }
                        if (registryObjectType == null) return false;

                        Class<?> registryObjectClass = null;
                        try {
                            registryObjectClass = (Class<?>) registryObjectType.getActualTypeArguments()[0];
                        } catch (ClassCastException e) {
                            log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName() + ".");
                            log.throwing(e);
                        }
                        if (registryObjectClass == null) return false;

                        return EntityType.class.isAssignableFrom(registryObjectClass);
                    })
                    .filter(field -> field.isAnnotationPresent(WithLootTables.class))
                    .map(field -> ReflectionUtils.getDeferredRegisterFromField(annotationData, field, EntityType.class))
                    .filter(Objects::nonNull)
                    .flatMap(blockDeferredRegister -> blockDeferredRegister.getEntries().stream())
                    .filter(RegistryObject::isPresent)
                    .filter(distinctBy(entityTypeRegistryObject -> entityTypeRegistryObject))
                    .map(RegistryObject::get);
            });
    }
}
