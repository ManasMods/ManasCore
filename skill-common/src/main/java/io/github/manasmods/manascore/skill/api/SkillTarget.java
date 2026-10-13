/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.skill.api;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Target of a skill activation that does not come from a key press, like mob AI.
 * The owner is turned towards it right before every press, held and release call.
 */
public class SkillTarget {
    @Getter
    @Nullable
    private final LivingEntity entity;
    @Nullable
    private final Vec3 position;
    protected SkillTarget(@Nullable LivingEntity entity, @Nullable Vec3 position) {
        this.entity = entity;
        this.position = position;
    }

    public static SkillTarget of(@NonNull LivingEntity entity) {
        return new SkillTarget(entity, null);
    }

    public static SkillTarget of(@NonNull Vec3 position) {
        return new SkillTarget(null, position);
    }

    public static SkillTarget of(@NonNull LivingEntity entity, @NonNull Vec3 position) {
        return new SkillTarget(entity, position);
    }

    /**
     * Used to get the aimed position: the fixed position if present, otherwise the eye position of the target entity.
     */
    public Vec3 getPosition() {
        if (this.position != null) return this.position;
        return this.entity.getEyePosition();
    }

    /**
     * Turns the user towards this target. Does nothing for self targets or for dead entity targets without a fixed position.
     */
    public void aim(LivingEntity user) {
        if (this.entity == user) return;
        if (this.position == null && !this.entity.isAlive()) return;
        user.lookAt(EntityAnchorArgument.Anchor.EYES, this.getPosition());
    }
}
