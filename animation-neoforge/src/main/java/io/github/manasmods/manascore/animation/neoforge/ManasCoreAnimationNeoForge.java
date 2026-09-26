/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.neoforge;

import io.github.manasmods.manascore.animation.ManasCoreAnimation;
import io.github.manasmods.manascore.animation.ModuleConstants;
import net.neoforged.fml.common.Mod;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreAnimationNeoForge {
    public ManasCoreAnimationNeoForge() {
        ManasCoreAnimation.init();
    }
}
