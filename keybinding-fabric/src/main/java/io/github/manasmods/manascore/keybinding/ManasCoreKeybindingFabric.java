/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybinding;

import net.fabricmc.api.ModInitializer;

public class ManasCoreKeybindingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreKeybinding.init();
    }
}