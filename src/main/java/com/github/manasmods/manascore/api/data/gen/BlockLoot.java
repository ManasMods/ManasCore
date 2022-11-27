/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.DoorDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.LeavesDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.OreDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.OtherDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.SelfDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.SlabDrop;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockLoot.WithLootTables;
import com.github.manasmods.manascore.api.util.ReflectionUtils;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AvailableSince("2.0.3.0")
@Log4j2
public abstract class BlockLoot extends net.minecraft.data.loot.BlockLoot {
    private static final Type GEN_ANNOTATION = Type.getType(GenerateBlockLoot.class);

    @NonExtendable
    @Override
    protected final void addTables() {
        loadTables();
        final List<AnnotationData> annotations = new ArrayList<>();
        ModList.get().forEachModFile(modFile -> {
            modFile.getScanResult().getAnnotations()
                .stream()
                .filter(annotationData -> GEN_ANNOTATION.equals(annotationData.annotationType()))
                .forEach(annotations::add);
        });
        generateAnnotationModels(annotations);
    }

    @NonExtendable
    private void generateAnnotationModels(List<AnnotationData> annotations) {
        annotations.forEach(annotationData -> {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(annotationData.clazz().getClassName());
            } catch (ClassNotFoundException e) {
                log.error("Could not load class " + annotationData.clazz().getClassName());
                log.throwing(e);
            }
            if (clazz == null) return;

            List<Field> blockRegistryObjectFieldList = Arrays.stream(clazz.getDeclaredFields())
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

                    return Block.class.isAssignableFrom(registryObjectClass);
                })
                .toList();

            generateDropSelfLootTables(annotationData, blockRegistryObjectFieldList);
            generateDropOtherLootTables(annotationData, blockRegistryObjectFieldList);
            generateDropOreLootTables(annotationData, blockRegistryObjectFieldList);
            generateDropLeavesLootTables(annotationData, blockRegistryObjectFieldList);
            generateDropDoorLootTables(annotationData, blockRegistryObjectFieldList);
            generateDropSlabLootTables(annotationData, blockRegistryObjectFieldList);
        });
    }

    @NonExtendable
    private void generateDropSelfLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(SelfDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;
            log.debug("Generating block loot for registry object {}", registryObject.getId());
            dropSelf(registryObject.get());
        }
    }

    @NonExtendable
    private void generateDropOtherLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(OtherDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;

            OtherDrop annotation = registryObjectField.getAnnotation(OtherDrop.class);
            if (annotation.value().isBlank()) continue;

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Item lootItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (lootItem == null) {
                log.error("Could not find loot item {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block loot for registry object {}", registryObject.getId());
            dropOther(registryObject.get(), lootItem);
        }
    }

    @NonExtendable
    private void generateDropOreLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(OreDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;

            OreDrop annotation = registryObjectField.getAnnotation(OreDrop.class);
            if (annotation.value().isBlank()) continue;

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Item lootItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (lootItem == null) {
                log.error("Could not find loot item {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block loot for registry object {}", registryObject.getId());
            add(registryObject.get(), block -> createOreDrop(registryObject.get(), lootItem));
        }
    }

    @NonExtendable
    private void generateDropLeavesLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(LeavesDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;

            LeavesDrop annotation = registryObjectField.getAnnotation(LeavesDrop.class);
            if (annotation.value().isBlank()) continue;

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Block saplingBlock = ForgeRegistries.BLOCKS.getValue(itemId);
            if (saplingBlock == null) {
                log.error("Could not find sapling block {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block loot for registry object {}", registryObject.getId());
            add(registryObject.get(), block -> createLeavesDrops(registryObject.get(), saplingBlock, annotation.chances()));
        }
    }

    @NonExtendable
    private void generateDropDoorLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(DoorDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;

            log.debug("Generating block loot for registry object {}", registryObject.getId());
            add(registryObject.get(), BlockLoot::createDoorTable);
        }
    }

    @NonExtendable
    private void generateDropSlabLootTables(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(SlabDrop.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;

            log.debug("Generating block loot for registry object {}", registryObject.getId());
            add(registryObject.get(), BlockLoot::createSlabItemTable);
        }
    }

    protected abstract void loadTables();

    /**
     * Gets all {@link DeferredRegister} objects in {@link GenerateBlockLoot} annotated classes with {@link WithLootTables} annotation.
     */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModList.get().getAllScanData()
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

                        return Block.class.isAssignableFrom(registryObjectClass);
                    })
                    .filter(field -> field.isAnnotationPresent(WithLootTables.class))
                    .map(field -> ReflectionUtils.getDeferredRegisterFromField(annotationData, field, Block.class))
                    .filter(Objects::nonNull)
                    .flatMap(blockDeferredRegister -> blockDeferredRegister.getEntries().stream())
                    .filter(RegistryObject::isPresent)
                    .filter(distinctBy(blockRegistryObject -> blockRegistryObject))
                    .map(RegistryObject::get);
            })
            .toList();
    }

    private static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
