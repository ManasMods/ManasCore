package com.github.manasmods.manascore.utils.neoforge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

public class RegistryLookupImpl {
    public static boolean isLivingEntityType(EntityType<?> entityType) {
        return DefaultAttributes.hasSupplier(entityType);
    }
}
