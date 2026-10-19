/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Cutscene {
    private final ResourceLocation id;
    private final int duration;
    private final List<CameraKeyframe> keyframes = new ArrayList<>();
    private String title = "";
    private String subtitle = "";
    private int titleStart;
    private int titleEnd;

    public Cutscene(ResourceLocation id, int duration) {
        this.id = id;
        this.duration = duration;
    }

    // offsets are local to the anchor: z points where the anchor faces, x to its left, y up
    public Cutscene keyframe(int tick, double x, double y, double z, double targetX, double targetY, double targetZ) {
        this.keyframes.add(new CameraKeyframe(tick, new Vec3(x, y, z), new Vec3(targetX, targetY, targetZ)));
        return this;
    }

    public Cutscene title(String title, String subtitle, int start, int end) {
        this.title = title;
        this.subtitle = subtitle;
        this.titleStart = start;
        this.titleEnd = end;
        return this;
    }

    public Vec3 getPositionAt(float time, Vec3 anchor, float yaw) {
        return anchor.add(this.sample(time, true).yRot(-yaw * Mth.DEG_TO_RAD));
    }

    public Vec3 getTargetAt(float time, Vec3 anchor, float yaw) {
        return anchor.add(this.sample(time, false).yRot(-yaw * Mth.DEG_TO_RAD));
    }

    private Vec3 sample(float time, boolean position) {
        int last = this.keyframes.size() - 1;
        if (time <= this.keyframes.getFirst().getTick()) return this.getPointOf(0, position);
        if (time >= this.keyframes.get(last).getTick()) return this.getPointOf(last, position);

        int index = 0;
        while (index < last - 1 && time >= this.keyframes.get(index + 1).getTick()) index++;
        int start = this.keyframes.get(index).getTick();
        float progress = (time - start) / (this.keyframes.get(index + 1).getTick() - start);

        Vec3 before = this.getPointOf(Math.max(index - 1, 0), position);
        Vec3 from = this.getPointOf(index, position);
        Vec3 to = this.getPointOf(index + 1, position);
        Vec3 after = this.getPointOf(Math.min(index + 2, last), position);
        return new Vec3(catmullRom(before.x, from.x, to.x, after.x, progress),
                catmullRom(before.y, from.y, to.y, after.y, progress),
                catmullRom(before.z, from.z, to.z, after.z, progress));
    }

    private Vec3 getPointOf(int index, boolean position) {
        CameraKeyframe keyframe = this.keyframes.get(index);
        return position ? keyframe.getPosition() : keyframe.getTarget();
    }

    private static double catmullRom(double before, double from, double to, double after, float progress) {
        float squared = progress * progress;
        return 0.5D * (2 * from + (to - before) * progress + (2 * before - 5 * from + 4 * to - after) * squared + (3 * from - before - 3 * to + after) * squared * progress);
    }
}
