/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.fabric;

import io.github.manasmods.manascore.cinematic.ManasCoreCinematic;
import net.fabricmc.api.ModInitializer;

public class ManasCoreCinematicFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreCinematic.init();
    }
}
