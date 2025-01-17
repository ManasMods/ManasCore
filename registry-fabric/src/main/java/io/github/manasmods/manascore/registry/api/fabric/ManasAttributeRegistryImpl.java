/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.registry.api.fabric;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManasAttributeRegistryImpl {
    private static final Map<Supplier<EntityType<? extends LivingEntity>>, Consumer<AttributeSupplier.Builder>> REGISTRY = new ConcurrentHashMap<>();
    private static final List<Consumer<AttributeSupplier.Builder>> GLOBAL_REGISTRY = new CopyOnWriteArrayList<>();
    private static final List<EntityType<? extends LivingEntity>> entityTypes = BuiltInRegistries.ENTITY_TYPE.stream()
            .filter(DefaultAttributes::hasSupplier).map(entityType -> (EntityType<? extends LivingEntity>) entityType)
            .collect(Collectors.toList());

    public static void register(Supplier<EntityType<? extends LivingEntity>> type, Consumer<AttributeSupplier.Builder> builder) {
        REGISTRY.put(type, builder);
    }

    public static void registerNew(Supplier<EntityType<? extends LivingEntity>> type, Supplier<AttributeSupplier.Builder> builder) {
        FabricDefaultAttributeRegistry.register(type.get(), builder.get());
    }

    public static void registerToAll(Consumer<AttributeSupplier.Builder> builder) {
        GLOBAL_REGISTRY.add(builder);
    }

    public static void init() {
        LifecycleEvent.SETUP.register(() -> {
            Multimap<EntityType<? extends LivingEntity>, Consumer<AttributeSupplier.Builder>> keyResolvedMap = ArrayListMultimap.create();
            // Map all keys to their resolved values
            REGISTRY.forEach((key, value) -> keyResolvedMap.put(key.get(), value));

            /*
            entityTypes.forEach(entityType -> {
                AttributeSupplier.Builder builder = new AttributeSupplier.Builder();
                // Apply existing attributes
                if (DefaultAttributes.hasSupplier(entityType)) {
                    DefaultAttributes.getSupplier(entityType).instances.forEach((attribute, attributeInstance) -> {
                        builder.add(attribute, attributeInstance.getBaseValue());
                    });
                }

                // Apply global custom attributes
                GLOBAL_REGISTRY.forEach(consumer -> consumer.accept(builder));
                // Apply specific custom attributes
                if (keyResolvedMap.containsKey(entityType)) keyResolvedMap.get(entityType).forEach(consumer -> consumer.accept(builder));
                // Register the attributes
                FabricDefaultAttributeRegistry.register(entityType, builder);
            });*/

            // Clear the registry
            REGISTRY.clear();
            GLOBAL_REGISTRY.clear();
        });
    }
}
