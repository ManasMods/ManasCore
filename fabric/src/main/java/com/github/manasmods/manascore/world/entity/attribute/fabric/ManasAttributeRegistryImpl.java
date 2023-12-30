package com.github.manasmods.manascore.world.entity.attribute.fabric;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.events.common.LifecycleEvent;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class ManasAttributeRegistryImpl {
    private static final Map<Supplier<EntityType<? extends LivingEntity>>, Consumer<Builder>> REGISTRY = new ConcurrentHashMap<>();
    private static final List<Consumer<Builder>> GLOBAL_REGISTRY = new CopyOnWriteArrayList<>();

    public static void register(Supplier<EntityType<? extends LivingEntity>> type, Consumer<Builder> builder) {
        REGISTRY.put(type, builder);
    }

    public static void registerToAll(Consumer<Builder> builder) {
        GLOBAL_REGISTRY.add(builder);
    }

    public static void init() {
        LifecycleEvent.SETUP.register(() -> {
            Multimap<EntityType<? extends LivingEntity>, Consumer<Builder>> keyResolvedMap = ArrayListMultimap.create();
            // Map all keys to their resolved values
            REGISTRY.forEach((key, value) -> keyResolvedMap.put(key.get(), value));

            keyResolvedMap.keySet().forEach(entityType -> {
                Builder builder = new Builder();
                // Apply existing attributes
                if (DefaultAttributes.hasSupplier(entityType)) {
                    DefaultAttributes.getSupplier(entityType).instances.forEach((attribute, attributeInstance) -> {
                        builder.add(attribute, attributeInstance.getBaseValue());
                    });
                }
                // Apply global custom attributes
                GLOBAL_REGISTRY.forEach(consumer -> consumer.accept(builder));
                // Apply specific custom attributes
                keyResolvedMap.get(entityType).forEach(consumer -> consumer.accept(builder));
                // Register the attributes
                FabricDefaultAttributeRegistry.register(entityType, builder);
            });

            // Clear the registry
            REGISTRY.clear();
            GLOBAL_REGISTRY.clear();
        });
    }
}
