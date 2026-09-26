/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.fabric;

import io.github.manasmods.manascore.testing.ManasCoreTesting;
import net.fabricmc.api.ModInitializer;

public class ManasCoreTestingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreTesting.init();
    }
}