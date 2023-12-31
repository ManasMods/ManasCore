package com.github.manasmods.manascore.api.skill;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;

public interface SkillEvents {
    Event<UnlockSkillEvent> UNLOCK_SKILL = EventFactory.createEventResult();
    Event<RemoveSkillEvent> REMOVE_SKILL = EventFactory.createEventResult();
    Event<SkillActivationEvent> ACTIVATE_SKILL = EventFactory.createEventResult();
    Event<SkillReleaseEvent> RELEASE_SKILL = EventFactory.createEventResult();
    Event<SkillToggleEvent> TOGGLE_SKILL = EventFactory.createEventResult();


    @FunctionalInterface
    interface UnlockSkillEvent {
        EventResult unlockSkill(ManasSkillInstance skillInstance, LivingEntity owner);
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
}
