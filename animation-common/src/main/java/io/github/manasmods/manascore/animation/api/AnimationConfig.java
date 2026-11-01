/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.api;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.api.Comment;
import io.github.manasmods.manascore.config.api.ManasConfig;

public class AnimationConfig extends ManasConfig {
    public String getFileName() {
        return "manascore/animation";
    }

    public static AnimationConfig get() {
        return ConfigRegistry.getConfig(AnimationConfig.class);
    }

    @Comment("Opacity of your own arms during a first-person animation, from 0 (invisible) to 1 (solid). Held items stay solid.")
    public float firstPersonOpacity = 0.5F;
}
