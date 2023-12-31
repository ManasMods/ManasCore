package com.github.manasmods.manascore.api.skill;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;

public interface SkillEvents {
    Event<UnlockSkillEvent> UNLOCK_SKILL = EventFactory.createEventResult();


    @FunctionalInterface
    interface UnlockSkillEvent {
        EventResult unlockSkill(ManasSkillInstance skillInstance, LivingEntity owner);
    }
}
