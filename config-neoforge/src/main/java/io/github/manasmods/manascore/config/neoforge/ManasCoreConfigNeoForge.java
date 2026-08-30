/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.config.neoforge;

import io.github.manasmods.manascore.config.ManasCoreConfig;
import io.github.manasmods.manascore.config.ModuleConstants;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public class ManasCoreConfigNeoForge {
    public ManasCoreConfigNeoForge() {
        ManasCoreConfig.init();
    }
}
