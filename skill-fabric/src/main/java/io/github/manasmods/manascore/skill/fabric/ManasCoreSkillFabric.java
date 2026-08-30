/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.fabric;

import io.github.manasmods.manascore.skill.ManasCoreSkill;
import net.fabricmc.api.ModInitializer;

public class ManasCoreSkillFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreSkill.init();
    }
}