/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.neoforge;

import io.github.manasmods.manascore.testing.ManasCoreTesting;
import io.github.manasmods.manascore.testing.ModuleConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreTestingNeoForge {
    public ManasCoreTestingNeoForge(IEventBus modEventBus) {
        ManasCoreTesting.init();
    }
}
