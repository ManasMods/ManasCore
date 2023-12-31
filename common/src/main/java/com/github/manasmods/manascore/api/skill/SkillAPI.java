package com.github.manasmods.manascore.api.skill;

import com.github.manasmods.manascore.skill.SkillRegistry;
import dev.architectury.registry.registries.Registrar;

public final class SkillAPI {
    public static Registrar<ManasSkill> getSkillRegistry() {
        return SkillRegistry.SKILLS;
    }
}
