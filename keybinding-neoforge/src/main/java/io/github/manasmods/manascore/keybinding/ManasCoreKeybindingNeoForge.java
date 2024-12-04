/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybinding;

import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreKeybindingNeoForge {
    public ManasCoreKeybindingNeoForge() {
        ManasCoreKeybinding.init();
    }
}
