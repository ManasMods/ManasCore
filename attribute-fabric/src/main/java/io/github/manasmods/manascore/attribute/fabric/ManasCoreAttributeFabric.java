/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.attribute.fabric;

import io.github.manasmods.manascore.attribute.ManasCoreAttribute;
import net.fabricmc.api.ModInitializer;

public class ManasCoreAttributeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreAttribute.init();
    }
}