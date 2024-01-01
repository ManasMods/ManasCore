package com.github.manasmods.manascore.api.world.entity;

import com.github.manasmods.manascore.utils.Changeable;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface EntityEvents {
    Event<LivingTickEvent> LIVING_PRE_TICK = EventFactory.createLoop();
    Event<LivingTickEvent> LIVING_POST_TICK = EventFactory.createLoop();
    Event<LivingChangeTargetEvent> LIVING_CHANGE_TARGET = EventFactory.createEventResult();
    Event<LivingAttackEvent> LIVING_ATTACK = EventFactory.createEventResult();


    @FunctionalInterface
    interface LivingTickEvent {
        void tick(LivingEntity entity);
    }

    @FunctionalInterface
    interface LivingChangeTargetEvent {
        EventResult changeTarget(LivingEntity entity, Changeable<LivingEntity> changeableTarget);
    }

    interface LivingAttackEvent {
        EventResult attack(LivingEntity entity, DamageSource source, float amount);
    }
}
