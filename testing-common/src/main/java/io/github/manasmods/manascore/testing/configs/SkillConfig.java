package io.github.manasmods.manascore.testing.configs;

import io.github.manasmods.manascore.config.api.ManasConfig;

public class SkillConfig extends ManasConfig {
    public float ironGolemDamageMultiplier = 100;
    public boolean instaKillCreeper = true;

    public String getParentPath() {
        return "config/manascore_test";
    }
}
