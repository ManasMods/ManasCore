/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * This is the Registry Object for Ticking Skills when a {@link ManasSkill} is held down in specific mode.
 */
public class TickingSkill {
    @Getter
    private int duration = 0;
    @Getter
    private final ManasSkill skill;
    @Getter
    private final int mode;
    @Getter
    private final int keyNumber;
    public TickingSkill(ManasSkill skill, int mode, int keyNumber) {
        this.skill = skill;
        this.mode = mode;
        this.keyNumber = keyNumber;
    }

    public boolean tick(SkillStorage storage, LivingEntity entity) {
        if (!entity.isAlive()) return false;
        Optional<ManasSkillInstance> optional = storage.getSkill(skill);
        if (optional.isEmpty()) return false;

        ManasSkillInstance instance = optional.get();
        if (this.reachedMaxDuration(instance, entity) || !instance.canActivateSkill(entity, this.mode)) {
            if (instance.shouldTriggerReleaseOnHeldInterrupt(entity, this.keyNumber, this.mode))
                storage.handleSkillRelease(instance, this.keyNumber, this.mode, true);
            return false;
        }
        return instance.onHeld(entity, this.duration++, this.mode);
    }

    public boolean reachedMaxDuration(ManasSkillInstance instance, LivingEntity entity) {
        int maxDuration = instance.getMaxHeldTime(entity);
        if (maxDuration == -1) return false;
        return this.duration >= maxDuration;
    }

    public boolean matches(ManasSkill skill, int mode) {
        return this.skill == skill && this.mode == mode;
    }

    public boolean matches(ManasSkill skill, int mode, int keyNumber) {
        return this.skill == skill && this.mode == mode && this.keyNumber == keyNumber;
    }

    public static void addTickingSkill(LivingEntity livingEntity, ManasSkill skill, int mode, int keyNumber) {
        SkillStorage storage = SkillAPI.getSkillsFrom(livingEntity);
        for (TickingSkill tickingSkill : storage.heldSkills) if (tickingSkill.matches(skill, mode)) return;
        storage.heldSkills.add(new TickingSkill(skill, mode, keyNumber));
    }

    public static boolean isTickingSkill(LivingEntity entity, ManasSkill skill, int mode) {
        SkillStorage storage = SkillAPI.getSkillsFrom(entity);
        for (TickingSkill tickingSkill : storage.heldSkills) {
            if (tickingSkill.matches(skill, mode)) return true;
        }
        return false;
    }

    public static boolean isTickingSkill(LivingEntity entity, ManasSkill skill) {
        SkillStorage storage = SkillAPI.getSkillsFrom(entity);
        for (TickingSkill tickingSkill : storage.heldSkills) {
            if (tickingSkill.getSkill() == skill) return true;
        }
        return false;
    }
}
