/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.neoforge;

import io.github.manasmods.manascore.cinematic.ManasCoreCinematic;
import io.github.manasmods.manascore.cinematic.ModuleConstants;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreCinematicNeoForge {
    public ManasCoreCinematicNeoForge() {
        ManasCoreCinematic.init();
    }
}
