package com.github.manasmods.manascore.api.skills.event;

import lombok.Getter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * This Event is fired by {@link LivingHurtEvent} after the Barrier Attribute takes effect.
 * Canceling this event will cancel {@link LivingHurtEvent} and every event following after it.
 * <p>
 */
@ApiStatus.AvailableSince("2.0.18.0")
@Cancelable
public class DamagePostBarrierEvent extends Event {
    @Getter
    private final LivingHurtEvent event;
    @Getter
    private final LivingEntity entity;
    @Getter
    private final DamageSource source;
    @Getter
    private float amount;
    public DamagePostBarrierEvent(LivingHurtEvent event) {
        this.event = event;
        this.entity = event.getEntity();
        this.source = event.getSource();
        this.amount = event.getAmount();
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}