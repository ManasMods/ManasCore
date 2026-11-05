/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import io.github.manasmods.manascore.animation.impl.HumanoidAnimationHelper;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkeletonModel.class)
public abstract class MixinSkeletonModel<T extends Mob & RangedAttackMob> {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At("HEAD"))
    private void manascore$setupPivot(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        HumanoidAnimationHelper.prepare((SkeletonModel<T>) (Object) this, entity);
    }

    // TAIL because skeletons change their arms after the humanoid setupAnim
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At("TAIL"))
    private void manascore$applyAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        HumanoidAnimationHelper.applyAnimation((SkeletonModel<T>) (Object) this, entity, ageInTicks, netHeadYaw, headPitch);
    }
}
