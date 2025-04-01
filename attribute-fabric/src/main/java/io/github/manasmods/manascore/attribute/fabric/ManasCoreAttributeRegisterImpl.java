/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.fabric;

import dev.architectury.event.events.common.LifecycleEvent;
import io.github.manasmods.manascore.attribute.api.ManasCoreAttributes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ManasCoreAttributeRegisterImpl {
    public static final List<Holder<Attribute>> GENERIC_REGISTRY = new CopyOnWriteArrayList<>();
    public static final List<Holder<Attribute>> PLAYER_REGISTRY = new CopyOnWriteArrayList<>();

    public static Holder<Attribute> registerToPlayers(Holder<Attribute> holder) {
        PLAYER_REGISTRY.add(holder);
        return holder;
    }

    public static Holder<Attribute> registerToGeneric(Holder<Attribute> holder) {
        GENERIC_REGISTRY.add(holder);
        return holder;
    }

    public static @NotNull Holder<Attribute> registerPlayerAttribute(String modID, String id, String name, double amount,
                                                                     double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Attribute attribute = new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment);
        Holder<Attribute> holder = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ManasCoreAttributes.getResourceKey(modID, id), attribute);
        return registerToPlayers(holder);
    }

    public static @NotNull Holder<Attribute> registerGenericAttribute(String modID, String id, String name, double amount,
                                                                      double min, double max, boolean syncable, Attribute.Sentiment sentiment) {
        Attribute attribute = new RangedAttribute(name, amount, min, max).setSyncable(syncable).setSentiment(sentiment);
        Holder<Attribute> holder = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ManasCoreAttributes.getResourceKey(modID, id), attribute);
        return registerToGeneric(holder);
    }

    public static void init() {
        LifecycleEvent.SETUP.register(() -> {
            AttributeSupplier.Builder builder = new AttributeSupplier.Builder();
            DefaultAttributes.getSupplier(EntityType.PLAYER).instances.forEach((attribute, attributeInstance) -> {
                builder.add(attribute, attributeInstance.getBaseValue());
            });

            PLAYER_REGISTRY.forEach(builder::add);
            GENERIC_REGISTRY.forEach(builder::add);
            FabricDefaultAttributeRegistry.register(EntityType.PLAYER, builder);
        });
    }
}
