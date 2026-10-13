/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.accessor;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * This gives access to the detached state of the {@link Camera}, which is true in third person.
 */
@Mixin(Camera.class)
public interface AccessorCamera {
    /**
     * Used to set if the {@link Camera} is treated as detached from its entity.
     */
    @Accessor
    void setDetached(boolean detached);
}
