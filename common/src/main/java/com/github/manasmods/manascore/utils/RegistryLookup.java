package com.github.manasmods.manascore.utils;

import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.function.Consumer;

public class RegistryLookup {
    public static void forEachLivingEntityType(final String lookupModId, Consumer<EntityType<? extends LivingEntity>> consumer) {
        for (EntityType<?> type : RegistrarManager.get(lookupModId).get(Registries.ENTITY_TYPE)) {
            //if (!isLivingEntityType(type)) continue;
            if (!DefaultAttributes.hasSupplier(type)) continue;
            consumer.accept((EntityType<? extends LivingEntity>) type);
        }
    }

//    @ExpectPlatform
//    public static boolean isLivingEntityType(EntityType<?> entityType) {
//        throw new AssertionError();
//    }
}
