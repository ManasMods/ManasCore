/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import io.github.manasmods.manascore.animation.impl.HumanoidAnimationHelper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Poses the {@link PlayerModel} from the active animation of the entity and advances its playback.
 * This covers every entity drawn with a player model, not only players.
 */
@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel<T extends LivingEntity> {
    @Inject(method = "setupAnim*", at = @At(value = "HEAD"))
    public void setupPivot(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        HumanoidAnimationHelper.prepare((PlayerModel<T>) (Object) this, entityIn);
    }

    @Inject(method = "setupAnim*", at = @At(value = "TAIL"))
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        PlayerModel<T> model = (PlayerModel<T>) (Object) this;
        HumanoidAnimationHelper.applyAnimation(model, entityIn, ageInTicks, netHeadYaw, headPitch);

        // Sync overlay layers
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.jacket.copyFrom(model.body);
    }
}
