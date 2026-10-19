/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api;

import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
public class CameraKeyframe {
    private final int tick;
    private final Vec3 position;
    private final Vec3 target;

    public CameraKeyframe(int tick, Vec3 position, Vec3 target) {
        this.tick = tick;
        this.position = position;
        this.target = target;
    }
}
