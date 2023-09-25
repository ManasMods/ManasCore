package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.manascore.capability.skill.event.TickEventListenerHandler;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class TickingSkill {
    private int duration = 0;
    private final int maxDuration;
    @Getter
    private final ManasSkill skill;
    public TickingSkill(ManasSkill skill) {
        this.skill = skill;
        this.maxDuration = skill.getMaxHeldTime();
    }

    public boolean tick(SkillStorage storage, LivingEntity entity) {
        Optional<ManasSkillInstance> optional = storage.getSkill(skill);
        if (optional.isEmpty()) return false;

        if (reachedMaxDuration()) {
            TickEventListenerHandler.tickingSkills.get(entity.getUUID()).remove(this);
            return false;
        }
        return optional.get().onHeld(entity, this.duration++);
    }

    public boolean reachedMaxDuration() {
        if (maxDuration == -1) return false;
        return duration >= maxDuration;
    }
}
