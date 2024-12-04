/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.registry.api.neoforge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class ManasAttributeRegistryImpl {
    private static final Map<Supplier<EntityType<? extends LivingEntity>>, Consumer<AttributeSupplier.Builder>> REGISTRY = new ConcurrentHashMap<>();
    private static final List<Consumer<AttributeSupplier.Builder>> GLOBAL_REGISTRY = new CopyOnWriteArrayList<>();
    private static final Map<Supplier<EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier.Builder>> NEW_REGISTRY = new ConcurrentHashMap<>();

    public static void register(Supplier<EntityType<? extends LivingEntity>> type, Consumer<AttributeSupplier.Builder> builder) {
        REGISTRY.put(type, builder);
    }

    public static void registerToAll(Consumer<AttributeSupplier.Builder> builder) {
        GLOBAL_REGISTRY.add(builder);
    }

    public static void registerNew(Supplier<EntityType<? extends LivingEntity>> type, Supplier<AttributeSupplier.Builder> builder) {
        NEW_REGISTRY.put(type, builder);
    }

    static void registerAttributes(final EntityAttributeModificationEvent e) {
        Multimap<EntityType<? extends LivingEntity>, Consumer<AttributeSupplier.Builder>> keyResolvedMap = ArrayListMultimap.create();
        // Map all keys to their resolved values
        REGISTRY.forEach((key, value) -> keyResolvedMap.put(key.get(), value));
        e.getTypes().forEach(type -> {
            AttributeSupplier.Builder builder = new AttributeSupplier.Builder();
            if (keyResolvedMap.containsKey(type)) keyResolvedMap.get(type).forEach(consumer -> consumer.accept(builder));
            GLOBAL_REGISTRY.forEach(consumer -> consumer.accept(builder));
            builder.build().instances.forEach((attribute, attributeInstance) -> e.add(type, attribute, attributeInstance.getBaseValue()));
        });

        // Clear the registry
        REGISTRY.clear();
        GLOBAL_REGISTRY.clear();
    }

    static void registerNewAttributes(final EntityAttributeCreationEvent e) {
        NEW_REGISTRY.forEach((key, value) -> e.put(key.get(), value.get().build()));
        NEW_REGISTRY.clear();
    }

    public static void init() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        modEventBus.addListener(ManasAttributeRegistryImpl::registerAttributes);
        modEventBus.addListener(ManasAttributeRegistryImpl::registerNewAttributes);
    }
}
