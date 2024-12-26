/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.register.api;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManasAttributeRegister {
    @ExpectPlatform
    public static void register(Supplier<EntityType<? extends LivingEntity>> type, Consumer<AttributeSupplier.Builder> builder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerToAll(Consumer<AttributeSupplier.Builder> builder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerNew(Supplier<EntityType<? extends LivingEntity>> type, Supplier<AttributeSupplier.Builder> builder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }
}
