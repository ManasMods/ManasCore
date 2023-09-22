package com.github.manasmods.manascore.api.skills.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event splits the {@link LivingHurtEvent} into three phases in a sorted order.
 * Canceling this event will cancel the Damage reduction.
 * <p>
 */
@ApiStatus.AvailableSince("2.0.18.0")
public class SkillDamageEvent extends Event {
    @Getter
    private final LivingHurtEvent event;
    @Getter
    private final LivingEntity entity;
    @Getter
    private final DamageSource source;
    @Getter
    @Setter
    private float amount;

    public SkillDamageEvent(LivingHurtEvent event) {
        this.event = event;
        this.entity = event.getEntity();
        this.source = event.getSource();
        this.amount = event.getAmount();
    }

    /**
     * This Event is fired by {@link LivingHurtEvent} before touching the barrier.
     * Canceling this event will cancel {@link LivingHurtEvent} and every event following after it.
     * <p>
     */
    public static class PreBarrier extends SkillDamageEvent {
        public PreBarrier(LivingHurtEvent event) {
            super(event);
        }
    }

    /**
     * This Event is fired by {@link LivingHurtEvent} after {@link PreBarrier} and before {@link PostBarrier}.
     * Canceling this event will cancel the Damage reduction.
     * <p>
     */
    public static class Barrier extends SkillDamageEvent {
        public Barrier(LivingHurtEvent event) {
            super(event);
        }
    }

    /**
     * This Event is fired by {@link LivingHurtEvent} after the damage passed the barrier and right before touching the player.
     * Canceling this event will cancel {@link LivingHurtEvent} and every event following after it.
     * <p>
     */
    public static class PostBarrier extends SkillDamageEvent {
        public PostBarrier(LivingHurtEvent event) {
            super(event);
        }
    }
}