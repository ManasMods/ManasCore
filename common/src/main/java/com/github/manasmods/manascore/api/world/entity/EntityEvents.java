package com.github.manasmods.manascore.api.world.entity;

import com.github.manasmods.manascore.utils.Changeable;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

public interface EntityEvents {
    Event<LivingTickEvent> LIVING_PRE_TICK = EventFactory.createLoop();
    Event<LivingTickEvent> LIVING_POST_TICK = EventFactory.createLoop();
    Event<LivingChangeTargetEvent> LIVING_CHANGE_TARGET = EventFactory.createEventResult();
    Event<LivingAttackEvent> LIVING_ATTACK = EventFactory.createEventResult();
    Event<LivingHurtEvent> LIVING_HURT = EventFactory.createEventResult();
    Event<LivingDamageEvent> LIVING_DAMAGE = EventFactory.createEventResult();
    Event<ProjectileHitEvent> PROJECTILE_HIT = EventFactory.createLoop();


    @FunctionalInterface
    interface LivingTickEvent {
        void tick(LivingEntity entity);
    }

    @FunctionalInterface
    interface LivingChangeTargetEvent {
        EventResult changeTarget(LivingEntity entity, Changeable<LivingEntity> changeableTarget);
    }

    @FunctionalInterface
    interface LivingAttackEvent {
        EventResult attack(LivingEntity entity, DamageSource source, float amount);
    }

    @FunctionalInterface
    interface LivingHurtEvent {
        EventResult hurt(LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }

    @FunctionalInterface
    interface LivingDamageEvent {
        EventResult damage(LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }

    @FunctionalInterface
    interface ProjectileHitEvent {
        void hit(HitResult hitResult, Projectile projectile,Changeable<ProjectileHitResult> result);
    }

    enum ProjectileHitResult {
        DEFAULT, // Hit, damage + possibly continue
        HIT, // Hit + damage
        HIT_NO_DAMAGE, // Hit
        PASS // Pass through
    }
}
