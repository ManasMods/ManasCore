/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.neoforge;

import io.github.manasmods.manascore.skill.ManasCoreSkill;
import io.github.manasmods.manascore.skill.ModuleConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreSkillNeoForge {
    public ManasCoreSkillNeoForge(IEventBus modEventBus) {
        ManasCoreSkill.init();
    }
}
