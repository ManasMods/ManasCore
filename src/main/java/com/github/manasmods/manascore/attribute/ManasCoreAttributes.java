/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.attribute;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ManasCoreAttributes {
    private static final DeferredRegister<Attribute> registry = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ManasCore.MOD_ID);
    public static final RegistryObject<Attribute> JUMP_POWER = registry.register("jump_power", () -> new RangedAttribute("manascore.attribute.max_aura.name", 0.42, 0, 800).setSyncable(true));

    public static void register(final IEventBus modEventBus) {
        registry.register(modEventBus);
    }
}
