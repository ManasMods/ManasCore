/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.neoforge;

import io.github.manasmods.manascore.animation.ManasCoreAnimation;
import io.github.manasmods.manascore.animation.ModuleConstants;
import net.neoforged.fml.common.Mod;

/**
 * This is the NeoForge entry point of the animation module.
 * It calls {@link ManasCoreAnimation#init()}.
 */
@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreAnimationNeoForge {
    public ManasCoreAnimationNeoForge() {
        ManasCoreAnimation.init();
    }
}
