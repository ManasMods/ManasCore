package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
public class TickingSkill {
    private int duration = 0;
    private final ManasSkill skill;
    public TickingSkill(ManasSkill skill) {
        this.skill = skill;
    }

    public @Nullable ManasSkillInstance getSkillInstance(SkillStorage storage, LivingEntity entity) {
        Optional<ManasSkillInstance> optional = storage.getSkill(this.getSkill());
        return optional.orElse(null);
    }

    public boolean tick(SkillStorage storage, LivingEntity entity) {
        ManasSkillInstance instance = this.getSkillInstance(storage, entity);
        if (instance == null) return false;
        if (reachedMaxDuration(instance, entity)) return false;

        if (!instance.canInteractSkill(entity)) {
            instance.onRelease(entity, this.getDuration());
            return false;
        }
        return instance.onHeld(entity, this.duration++);
    }

    public boolean reachedMaxDuration(ManasSkillInstance instance, LivingEntity entity) {
        int maxDuration = instance.getMaxHeldTime(entity);
        if (maxDuration == -1) return false;
        return this.getDuration() >= maxDuration;
    }
}
