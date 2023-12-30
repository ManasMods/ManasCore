package com.github.manasmods.manascore.api.world.entity;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public interface EntityEvents {
    Event<LivingAttributeCreation> LIVING_ATTRIBUTE_CREATION = EventFactory.createLoop();

    @FunctionalInterface
    interface LivingAttributeCreation {
        void createAttributes(AttributeSupplier.Builder builder);
    }
}
