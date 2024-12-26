/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybind.fabric;

import io.github.manasmods.manascore.keybind.ManasCoreKeybind;
import net.fabricmc.api.ModInitializer;

public class ManasCoreKeybindFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreKeybind.init();
    }
}