/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.registry;

import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreRegistryNeoForge {
    public ManasCoreRegistryNeoForge() {
        ManasCoreRegistry.init();
    }
}
