package io.github.manasmods.manascore.testing.configs;

import io.github.manasmods.manascore.config.api.Comment;
import io.github.manasmods.manascore.config.api.ManasConfig;

public class SkillConfig extends ManasConfig {
    @Comment("Multiplier for Iron Golem damage.")
    public float ironGolemDamageMultiplier = 100;

    @Comment("Enables instant kill for creepers.\nSet to true to set the creeper's HP to 0.")
    public boolean instaKillCreeper = true;

    public String getFileName() {
        return "manascore_test/skill_config";
    }
}
