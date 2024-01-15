package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.utils.Changeable;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface SkillEvents {
    Event<UnlockSkillEvent> UNLOCK_SKILL = EventFactory.createEventResult();
    Event<RemoveSkillEvent> REMOVE_SKILL = EventFactory.createEventResult();
    Event<SkillActivationEvent> ACTIVATE_SKILL = EventFactory.createEventResult();
    Event<SkillReleaseEvent> RELEASE_SKILL = EventFactory.createEventResult();
    Event<SkillToggleEvent> TOGGLE_SKILL = EventFactory.createEventResult();
    Event<SkillTickEvent> SKILL_PRE_TICK = EventFactory.createEventResult();
    Event<SkillPostTickEvent> SKILL_POST_TICK = EventFactory.createLoop();
    Event<SkillScrollEvent> SKILL_SCROLL = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_PRE_CALCULATION = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_CALCULATION = EventFactory.createEventResult();
    Event<SkillDamageCalculationEvent> SKILL_DAMAGE_POST_CALCULATION = EventFactory.createEventResult();


    @FunctionalInterface
    interface UnlockSkillEvent {
        EventResult unlockSkill(ManasSkillInstance skillInstance, LivingEntity owner, Changeable<Component> unlockMessage);
    }

    @FunctionalInterface
    interface RemoveSkillEvent {
        EventResult removeSkill(ManasSkillInstance skillInstance, LivingEntity owner);
    }

    @FunctionalInterface
    interface SkillActivationEvent {
        EventResult activateSkill(ManasSkillInstance skillInstance, LivingEntity owner, int keyNumber);
    }

    @FunctionalInterface
    interface SkillReleaseEvent {
        EventResult releaseSkill(ManasSkillInstance skillInstance, LivingEntity owner, int keyNumber, int heldTicks);
    }

    @FunctionalInterface
    interface SkillToggleEvent {
        EventResult toggleSkill(ManasSkillInstance skillInstance, LivingEntity owner);
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
    interface SkillScrollEvent {
        EventResult scroll(ManasSkillInstance skillInstance, LivingEntity owner, double delta);
    }

    @FunctionalInterface
    interface SkillDamageCalculationEvent {
        EventResult calculate(Skills storage, LivingEntity entity, DamageSource source, Changeable<Float> amount);
    }
}
