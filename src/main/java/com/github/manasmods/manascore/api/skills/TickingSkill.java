package com.github.manasmods.manascore.api.skills;

import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class TickingSkill {
    private int duration = 0;
    @Getter
    private final ResourceLocation skillId;
    public TickingSkill(ResourceLocation id) {
        this.skillId = id;
    }

    public boolean tick(SkillStorage storage, LivingEntity entity) {
        ManasSkill manasSkill = SkillAPI.getSkillRegistry().getValue(this.skillId);
        if (manasSkill == null) return false;

        Optional<ManasSkillInstance> optional = storage.getSkill(manasSkill);
        if (optional.isEmpty()) return false;

        return optional.get().onHeld(entity, this.duration++);
    }
}
