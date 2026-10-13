/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.fabric;

import io.github.manasmods.manascore.animation.ManasCoreAnimation;
import net.fabricmc.api.ModInitializer;

/**
 * This is the Fabric entry point of the animation module.
 * It calls {@link ManasCoreAnimation#init()}.
 */
public class ManasCoreAnimationFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCoreAnimation.init();
    }
}
