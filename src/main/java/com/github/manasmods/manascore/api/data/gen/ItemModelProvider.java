/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import com.github.manasmods.manascore.api.data.gen.annotation.GenerateItemModels;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateItemModels.SingleHandheldTextureModel;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateItemModels.SingleTextureModel;
import com.github.manasmods.manascore.api.util.ReflectionUtils;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AvailableSince("1.0.0.0")
@SuppressWarnings("unused")
@Log4j2
public abstract class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    private static final Type GEN_MODELS = Type.getType(GenerateItemModels.class);

    public ItemModelProvider(final GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @OverrideOnly
    protected abstract void generate();

    @NonExtendable
    @Override
    protected final void registerModels() {
        generate();

        final List<AnnotationData> annotations = new ArrayList<>();
        ModList.get().forEachModFile(modFile -> {
            modFile.getScanResult().getAnnotations()
                .stream()
                .filter(annotationData -> GEN_MODELS.equals(annotationData.annotationType()))
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

            List<Field> itemRegistryObjectFieldList = Arrays.stream(clazz.getDeclaredFields())
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

                    return Item.class.isAssignableFrom(registryObjectClass);
                })
                .toList();

            generateSingleTextureModels(annotationData, itemRegistryObjectFieldList);
            generateSingleHandheldTextureModels(annotationData, itemRegistryObjectFieldList);
        });
    }

    @NonExtendable
    private void generateSingleTextureModels(AnnotationData annotationData, List<Field> itemRegistryObjectFieldList) {
        for (Field registryObjectField : itemRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(SingleTextureModel.class)) continue;
            RegistryObject<Item> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Item.class);

            if (registryObject == null) continue;
            SingleTextureModel annotation = registryObjectField.getAnnotation(SingleTextureModel.class);
            // Generate default model
            if (annotation.value().isEmpty() || annotation.value().isBlank()) {
                log.debug("Generating item model for registry object {}", registryObject.getId());
                singleTexture(registryObject.get());
                continue;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Item textureItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (textureItem == null) {
                log.error("Could not find texture item {} for item {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating item model for registry object {} with texture of {}", registryObject.getId(), itemId);
            singleTexture(registryObject.get(), textureItem);
        }
    }

    @NonExtendable
    private void generateSingleHandheldTextureModels(AnnotationData annotationData, List<Field> itemRegistryObjectFieldList) {
        for (Field registryObjectField : itemRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(SingleHandheldTextureModel.class)) continue;
            RegistryObject<Item> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Item.class);

            if (registryObject == null) continue;
            SingleHandheldTextureModel annotation = registryObjectField.getAnnotation(SingleHandheldTextureModel.class);
            // Generate default model
            if (annotation.value().isEmpty() || annotation.value().isBlank()) {
                log.debug("Generating item model for registry object {}", registryObject.getId());
                handheldSingleTexture(registryObject.get());
                continue;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Item textureItem = ForgeRegistries.ITEMS.getValue(itemId);
            if (textureItem == null) {
                log.error("Could not find texture item {} for item {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating item model for registry object {} with texture of {}", registryObject.getId(), itemId);
            handheldSingleTexture(registryObject.get(), textureItem);
        }
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final String name(Item item) {
        return rl(item).getPath();
    }

    /**
     * Generates the item model json file.
     * Can be used for any default rendered {@link Item} object.
     *
     * @param item the target {@link Item}
     */
    protected void singleTexture(Item item) {
        singleTexture(item, item);
    }

    /**
     * Generates the item model json file.
     * Can be used for any default rendered {@link Item} object.
     *
     * @param item        the target {@link Item}
     * @param textureItem the texture providing {@link Item}
     */
    protected void singleTexture(Item item, Item textureItem) {
        getBuilder(name(item))
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
            .texture("layer0", new ResourceLocation(rl(textureItem).getNamespace(), "item/" + name(textureItem)));
    }

    /**
     * Generates the item model json file.
     * Can be used for any handheld rendered {@link Item} like Tools and Torches.
     *
     * @param item the target {@link Item}
     */
    protected void handheldSingleTexture(Item item) {
        handheldSingleTexture(item, item);
    }

    /**
     * Generates the item model json file.
     * Can be used for any handheld rendered {@link Item} like Tools and Torches.
     *
     * @param item        the target {@link Item}
     * @param textureItem the texture providing {@link Item}
     */
    protected void handheldSingleTexture(Item item, Item textureItem) {
        getBuilder(name(item))
            .parent(new ModelFile.UncheckedModelFile(mcLoc("item/handheld")))
            .texture("layer0", new ResourceLocation(rl(textureItem).getNamespace(), "item/" + name(textureItem)));
    }
}
