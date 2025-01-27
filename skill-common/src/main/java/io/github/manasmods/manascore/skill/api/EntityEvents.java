/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;

public class EntityEvents {
    public static Event<LivingTickEvent> LIVING_PRE_TICK = EventFactory.createLoop();
    public static Event<LivingTickEvent> LIVING_POST_TICK = EventFactory.createLoop();
    public static Event<LivingEffectAddedEvent> LIVING_EFFECT_ADDED = EventFactory.createEventResult();
    public static Event<LivingChangeTargetEvent> LIVING_CHANGE_TARGET = EventFactory.createEventResult();
    public static Event<LivingHurtEvent> LIVING_HURT = EventFactory.createEventResult();
    public static Event<LivingDamageEvent> LIVING_DAMAGE = EventFactory.createEventResult();
    public static Event<ProjectileHitEvent> PROJECTILE_HIT = EventFactory.createLoop();


    @FunctionalInterface
    public interface LivingTickEvent {
        void tick(LivingEntity entity);
    }

    @FunctionalInterface
    public interface LivingEffectAddedEvent {
        EventResult effectAdd(LivingEntity entity, Entity source, Changeable<MobEffectInstance> changeableInstance);
    }

    @FunctionalInterface
    public interface LivingChangeTargetEvent {
        EventResult changeTarget(LivingEntity entity, Changeable<LivingEntity> changeableTarget);
    }

    @FunctionalInterface
    public interface LivingHurtEvent {
        EventResult hurt(LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }

    @FunctionalInterface
    public interface LivingDamageEvent {
        EventResult damage(LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }

    @FunctionalInterface
    public interface ProjectileHitEvent {
        void hit(HitResult hitResult, Projectile projectile, Changeable<ProjectileDeflection> deflection, Changeable<ProjectileHitResult> result);
    }

    public enum ProjectileHitResult {
        DEFAULT, // Hit, damage + possibly continue
        HIT, // Hit + damage
        HIT_NO_DAMAGE, // Hit
        PASS // Pass through
    }
}
