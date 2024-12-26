/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.network.neoforge;

import io.github.manasmods.manascore.keybind.ManasCoreKeybind;
import io.github.manasmods.manascore.keybind.ModuleConstants;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreKeybindNeoForge {
    public ManasCoreKeybindNeoForge() {
        ManasCoreKeybind.init();
    }
}
