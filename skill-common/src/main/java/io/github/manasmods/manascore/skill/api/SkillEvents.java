/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import io.github.manasmods.manascore.network.api.util.Changeable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface SkillEvents {
    Event<UnlockSkillEvent> UNLOCK_SKILL = EventFactory.createEventResult();
    Event<RemoveSkillEvent> REMOVE_SKILL = EventFactory.createEventResult();
    Event<SkillActivationEvent> ACTIVATE_SKILL = EventFactory.createEventResult();
    Event<SkillReleaseEvent> RELEASE_SKILL = EventFactory.createEventResult();
    Event<SkillToggleEvent> TOGGLE_SKILL = EventFactory.createEventResult();
    Event<SkillScrollEvent> SKILL_SCROLL = EventFactory.createEventResult();
    Event<SkillScrollClientEvent> SKILL_SCROLL_CLIENT = EventFactory.createEventResult();
    Event<SkillTickEvent> SKILL_PRE_TICK = EventFactory.createEventResult();
    Event<SkillPostTickEvent> SKILL_POST_TICK = EventFactory.createLoop();
    Event<SkillUpdateCooldownEvent> SKILL_UPDATE_COOLDOWN = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_PRE_CALCULATION = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_CALCULATION = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_POST_CALCULATION = EventFactory.createEventResult();


    @FunctionalInterface
    interface UnlockSkillEvent {
        EventResult unlockSkill(ManasSkillInstance skillInstance, LivingEntity owner, Changeable<MutableComponent> unlockMessage);
    }

    @FunctionalInterface
    interface RemoveSkillEvent {
        EventResult removeSkill(ManasSkillInstance skillInstance, LivingEntity owner, Changeable<MutableComponent> forgetMessage);
    }

    @FunctionalInterface
    interface SkillActivationEvent {
        EventResult activateSkill(Changeable<ManasSkillInstance> skillInstance, LivingEntity owner, int keyNumber, int mode);
    }

    @FunctionalInterface
    interface SkillReleaseEvent {
        EventResult releaseSkill(Changeable<ManasSkillInstance> skillInstance, LivingEntity owner, int keyNumber, int mode, int heldTicks);
    }

    @FunctionalInterface
    interface SkillToggleEvent {
        EventResult toggleSkill(Changeable<ManasSkillInstance> skillInstance, LivingEntity owner);
    }

    @FunctionalInterface
    interface SkillScrollEvent {
        EventResult scroll(Changeable<ManasSkillInstance> skillInstance, LivingEntity owner, Changeable<Double> delta);
    }

    @FunctionalInterface
    interface SkillScrollClientEvent {
        EventResult scroll(ManasSkillInstance skillInstance, LivingEntity owner, double delta);
    }

    @FunctionalInterface
    interface SkillTickEvent {
        EventResult tick(ManasSkillInstance skillInstance, LivingEntity owner);
    }

    @FunctionalInterface
    interface SkillPostTickEvent {
        void tick(ManasSkillInstance skillInstance, LivingEntity owner);
    }

    @FunctionalInterface
    interface SkillUpdateCooldownEvent {
        EventResult cooldown(ManasSkillInstance skillInstance, LivingEntity owner, int currentCooldown, int mode);
    }

    @FunctionalInterface
    interface SkillDamageCalculationEvent {
        EventResult calculate(Skills storage, LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }
}
