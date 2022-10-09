package com.github.manasmods.manascore.api.skills.variants;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;

public class ToggleableSkill extends ManasSkill {
    @Override
    public ManasSkillInstance createDefaultInstance() {
        return new ToggleableSkillInstance(this);
    }
}
