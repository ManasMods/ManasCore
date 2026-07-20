/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRendererAnimation extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();

    public MixinPlayerRendererAnimation(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void hideBonesInFirstPerson(AbstractClientPlayer entity, float f, float g, PoseStack poseStack,
                                                  MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (PlayerAnimationAPI.state(entity).firstPerson && !PlayerAnimationAPI.renderingGuiEntity
                && manascore$mc.options.getCameraType().isFirstPerson() && entity == manascore$mc.player && manascore$mc.screen == null) {
            this.model.head.visible = false;
            this.model.body.visible = false;
            this.model.leftLeg.visible = false;
            this.model.rightLeg.visible = false;
            this.model.rightArm.visible = false;
            this.model.leftArm.visible = false;
            this.model.hat.visible = false;
            this.model.leftSleeve.visible = false;
            this.model.rightSleeve.visible = false;
            this.model.leftPants.visible = false;
            this.model.rightPants.visible = false;
            this.model.jacket.visible = false;
            this.model.rightArm.visible = true;
            this.model.leftArm.visible = true;
        }
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
    private void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float f, float bodyYaw,
                                          float deltaTick, float g, CallbackInfo ci) {
        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(player);
        if (animation == null) return;
        PlayerAnimationAPI.PlayerBone bone = animation.bones.get("body");
        if (bone == null) return;

        PlayerAnimationAPI.PlayerAnimationState data = PlayerAnimationAPI.state(player);
        boolean firstPerson = data.firstPerson && !PlayerAnimationAPI.renderingGuiEntity && manascore$mc.options.getCameraType().isFirstPerson() && player == manascore$mc.player && manascore$mc.screen == null;
        float animationProgress = data.progress;
        Vec3 scale = PlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress, player);
        if (scale != null) poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);

        Vec3 position = PlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress, player);
        if (position != null) {
            if (!firstPerson) poseStack.translate((float) -position.x * 0.0625f, (float) (position.y * 0.0625f) + 0.75f, (float) position.z * 0.0625f);
        }

        Vec3 rotation = PlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress, player);
        if (rotation != null) {
            if (!firstPerson) poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) -rotation.y));
            if (!firstPerson) poseStack.mulPose(Axis.XP.rotationDegrees((float) -rotation.x));
        }

        if (position != null) {
            if (!firstPerson) poseStack.translate(0, -0.75f, 0);
        }
    }
}
