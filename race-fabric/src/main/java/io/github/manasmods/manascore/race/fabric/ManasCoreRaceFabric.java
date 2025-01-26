/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race.fabric;

import io.github.manasmods.manascore.race.ManasCoreRace;
import net.fabricmc.api.ModInitializer;

public class ManasCoreRaceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreRace.init();
    }
}