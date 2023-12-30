package com.github.manasmods.manascore.world.entity.attribute;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Supplier;

public class ManasAttributeRegistry {
    @ExpectPlatform
    public static void register(Supplier<EntityType<? extends LivingEntity>> type, Supplier<AttributeSupplier.Builder> builderSupplier) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerToAll(Supplier<AttributeSupplier.Builder> builderSupplier) {
        throw new AssertionError();
    }
}
