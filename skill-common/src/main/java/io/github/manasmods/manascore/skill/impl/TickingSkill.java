/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.impl;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This is the Registry Object for Ticking Skills when a {@link ManasSkill} is held down in specific mode.
 */
public class TickingSkill {
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
        if (this.reachedMaxDuration(instance, entity) || !instance.canInteractSkill(entity)) {
            if (instance.shouldTriggerReleaseOnHeldInterrupt(entity, keyNumber, mode))
                storage.handleSkillRelease(instance, this.duration, this.keyNumber, this.mode, true);
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

    public static void addTickingSkill(Player player, ManasSkill skill, int mode, int keyNumber) {
        UUID uuid = player.getUUID();
        Collection<TickingSkill> skills = SkillStorage.tickingSkills.get(uuid);
        for (TickingSkill tickingSkill : skills) if (tickingSkill.matches(skill, mode)) return;
        SkillStorage.tickingSkills.put(uuid, new TickingSkill(skill, mode, keyNumber));
    }

    public static boolean isTickingSkill(LivingEntity entity, ManasSkill skill, int mode) {
        UUID uuid = entity.getUUID();
        for (TickingSkill tickingSkill : SkillStorage.tickingSkills.get(uuid)) {
            if (tickingSkill.matches(skill, mode)) return true;
        }
        return false;
    }

    public static boolean isTickingSkill(LivingEntity entity, ManasSkill skill) {
        UUID uuid = entity.getUUID();
        for (TickingSkill tickingSkill : SkillStorage.tickingSkills.get(uuid)) {
            if (tickingSkill.getSkill() == skill) return true;
        }
        return false;
    }
}
