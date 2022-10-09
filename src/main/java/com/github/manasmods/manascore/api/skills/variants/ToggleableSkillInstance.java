package com.github.manasmods.manascore.api.skills.variants;

import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;

public class ToggleableSkillInstance extends ManasSkillInstance {
    @Getter
    @Setter
    private boolean active;

    public ToggleableSkillInstance(ToggleableSkill skill) {
        super(skill);
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        tag.putBoolean("toggle.active", this.active);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        this.active = tag.getBoolean("toggle.active");
    }
}
