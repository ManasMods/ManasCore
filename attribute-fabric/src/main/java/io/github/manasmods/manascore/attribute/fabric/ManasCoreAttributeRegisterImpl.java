/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.fabric;

import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ManasCoreAttributeRegisterImpl {
    private static final List<Holder<Attribute>> GENERIC_REGISTRY = new CopyOnWriteArrayList<>();
    private static final List<Holder<Attribute>> PLAYER_REGISTRY = new CopyOnWriteArrayList<>();
    private static final List<EntityType<? extends LivingEntity>> entityTypes = BuiltInRegistries.ENTITY_TYPE.stream()
            .filter(DefaultAttributes::hasSupplier).map(entityType -> (EntityType<? extends LivingEntity>) entityType)
            .collect(Collectors.toList());

    public static void registerToPlayers(Holder<Attribute> holder) {
        PLAYER_REGISTRY.add(holder);
    }

    public static void registerToGeneric(Holder<Attribute> holder) {
        GENERIC_REGISTRY.add(holder);
    }

    public static void init() {
        LifecycleEvent.SETUP.register(() -> {
            entityTypes.forEach(entityType -> {
                if (entityType == null) return;
                AttributeSupplier.Builder builder = new AttributeSupplier.Builder();
                // Apply existing attributes
                if (DefaultAttributes.hasSupplier(entityType)) {
                    DefaultAttributes.getSupplier(entityType).instances.forEach((attribute, attributeInstance) -> {
                        builder.add(attribute, attributeInstance.getBaseValue());
                    });
                }

                if (entityType.equals(EntityType.PLAYER)) PLAYER_REGISTRY.forEach(builder::add);
                // Apply global custom attributes
                GENERIC_REGISTRY.forEach(builder::add);
                // Register the attributes
                FabricDefaultAttributeRegistry.register(entityType, builder);
            });

            // Clear the registry
            PLAYER_REGISTRY.clear();
            GENERIC_REGISTRY.clear();
        });
    }
}
