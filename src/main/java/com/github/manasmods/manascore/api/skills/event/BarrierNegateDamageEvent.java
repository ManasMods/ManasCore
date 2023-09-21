package com.github.manasmods.manascore.api.skills.event;

import lombok.Getter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired by {@link LivingHurtEvent} right after {@link DamagePreBarrierEvent} and before {@link DamagePostBarrierEvent}.
 * Canceling this event will cancel the Damage reduction.
 * <p>
 */
@ApiStatus.AvailableSince("2.0.18.0")
public class BarrierNegateDamageEvent extends Event {
    @Getter
    private final LivingHurtEvent event;
    @Getter
    private final LivingEntity entity;
    @Getter
    private final DamageSource source;
    @Getter
    private float amount;

    public BarrierNegateDamageEvent(LivingHurtEvent event) {
        this.event = event;
        this.entity = event.getEntity();
        this.source = event.getSource();
        this.amount = event.getAmount();
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}