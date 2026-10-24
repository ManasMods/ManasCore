/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.team.neoforge;

import io.github.manasmods.manascore.team.ManasCoreTeam;
import io.github.manasmods.manascore.team.ModuleConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge entry point of the team module.
 */
@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreTeamNeoForge {
    public ManasCoreTeamNeoForge(IEventBus modEventBus) {
        ManasCoreTeam.init();
    }
}
