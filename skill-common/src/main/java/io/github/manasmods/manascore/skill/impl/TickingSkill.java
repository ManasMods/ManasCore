/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.Skills;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * This is the Registry Object for Ticking Skills when a {@link ManasSkill} is held down in specific mode.
 */
public class TickingSkill {
    private int duration = 0;
    @Getter
    private final ManasSkill skill;
    @Getter
    private final int mode;
    public TickingSkill(ManasSkill skill, int mode) {
        this.skill = skill;
        this.mode = mode;
    }

    public boolean tick(Skills storage, LivingEntity entity) {
        if (!entity.isAlive()) return false;
        Optional<ManasSkillInstance> optional = storage.getSkill(skill);
        if (optional.isEmpty()) return false;

        ManasSkillInstance instance = optional.get();
        if (reachedMaxDuration(instance, entity)) return false;

        if (!instance.canInteractSkill(entity)) return false;
        return instance.onHeld(entity, this.duration++, mode);
    }

    public boolean reachedMaxDuration(ManasSkillInstance instance, LivingEntity entity) {
        int maxDuration = instance.getMaxHeldTime(entity);
        if (maxDuration == -1) return false;
        return duration >= maxDuration;
    }
}
