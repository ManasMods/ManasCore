/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus;

public class ManasCoreAttributes {
    private static final DeferredRegister<Attribute> registry = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ManasCore.MOD_ID);
    public static final RegistryObject<Attribute> CRIT_CHANCE = registry.register("crit_chance",
            () -> new RangedAttribute("manascore.attribute.crit_chance.name", 0, 0, 100).setSyncable(true));
    public static final RegistryObject<Attribute> CRIT_MULTIPLIER = registry.register("crit_multiplier",
            () -> new RangedAttribute("manascore.attribute.crit_multiplier.name", 1.5, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> JUMP_POWER = registry.register("jump_power",
            () -> new RangedAttribute("manascore.attribute.jump_power.name", 0.42, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> MINING_SPEED_MULTIPLIER = registry.register("mining_speed_multiplier",
            () -> new RangedAttribute("manascore.attribute.mining_speed.name", 1.0, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> SPRINTING_SPEED_MULTIPLIER = registry.register("sprinting_speed_multiplier",
            () -> new RangedAttribute("manascore.attribute.sprinting_speed.name", 1.0, 0, 1024).setSyncable(true));
    public static RegistryObject<Attribute> SWEEP_CHANCE = registry.register("sweep_chance",
            () -> new RangedAttribute("manascore.attribute.sweep_chance.name", 0, 0, 100).setSyncable(true));

    @ApiStatus.Internal
    public static void register(final IEventBus modEventBus) {
        registry.register(modEventBus);
        modEventBus.addListener(ManasCoreAttributes::applyAttributesToEntities);
    }

    private static void applyAttributesToEntities(final EntityAttributeModificationEvent e) {
        e.add(EntityType.PLAYER, JUMP_POWER.get());
        e.add(EntityType.PLAYER, MINING_SPEED_MULTIPLIER.get());
        e.add(EntityType.PLAYER, SWEEP_CHANCE.get());
        e.getTypes().forEach(type -> {
            e.add(type, CRIT_CHANCE.get());
            e.add(type, CRIT_MULTIPLIER.get());
            e.add(type, SPRINTING_SPEED_MULTIPLIER.get());
        });
    }
}
