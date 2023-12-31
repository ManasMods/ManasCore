package com.github.manasmods.manascore.api.world.entity;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

public interface EntityEvents {
    Event<LivingTickEvent> LIVING_PRE_TICK = EventFactory.createLoop();
    Event<LivingTickEvent> LIVING_POST_TICK = EventFactory.createLoop();


    @FunctionalInterface
    interface LivingTickEvent {
        void tick(LivingEntity entity);
    }
}
