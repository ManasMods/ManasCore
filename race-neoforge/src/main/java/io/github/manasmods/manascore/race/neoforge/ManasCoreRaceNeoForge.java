/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.neoforge;

import io.github.manasmods.manascore.race.ManasCoreRace;
import io.github.manasmods.manascore.race.ModuleConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreRaceNeoForge {
    public ManasCoreRaceNeoForge(IEventBus modEventBus) {
        ManasCoreRace.init();
    }
}
