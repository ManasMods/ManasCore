/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.neoforge;

import io.github.manasmods.manascore.skill.ManasCoreSkill;
import io.github.manasmods.manascore.skill.ModuleConstants;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreSkillNeoForge {
    public ManasCoreSkillNeoForge() {
        ManasCoreSkill.init();
        var neoEventBus = NeoForge.EVENT_BUS;
    }
}
