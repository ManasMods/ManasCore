/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.mixin;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow private boolean detached;

    @Shadow protected abstract void setPosition(Vec3 position);

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void manascore$setupCutscene(BlockGetter level, Entity entity, boolean thirdPerson, boolean mirrored, float partialTick, CallbackInfo ci) {
        if (!CutsceneClientHandler.isPlaying()) return;
        Vec3 target = CutsceneClientHandler.getCameraTarget(partialTick);
        Vec3 position = CutsceneClientHandler.getCameraPosition(level, target, partialTick);
        Vec3 look = target.subtract(position);
        this.detached = true;
        this.setPosition(position);
        this.setRotation((float) (Mth.atan2(-look.x, look.z) * Mth.RAD_TO_DEG) + CutsceneClientHandler.getShake(5, 1),
                (float) (-Mth.atan2(look.y, look.horizontalDistance()) * Mth.RAD_TO_DEG) + CutsceneClientHandler.getShake(3, 2));
    }
}
