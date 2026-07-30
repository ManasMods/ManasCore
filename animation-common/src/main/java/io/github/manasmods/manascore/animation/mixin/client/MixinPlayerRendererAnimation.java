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

    /**
     * Squares the body up with the head for animations declaring {@code manascore:aim_body}, so the whole
     * player turns toward what they are looking at instead of only the listed bones.
     * <p>
     * This has to run at {@code HEAD}, before {@code LivingEntityRenderer#render} reads {@code yBodyRot} to
     * derive the {@code netHeadYaw} it hands to the model. Doing it later - in {@code setupAnim}, where the
     * first-person path does the same thing - would only take effect on the following frame.
     * <p>
     * Yaw only, by design: the body never pitches. The bones in {@code manascore:aim_bones} carry the
     * vertical, and because {@code netHeadYaw} is head-minus-body it collapses to zero once this has run,
     * so those bones contribute pitch alone without needing to know this happened.
     */
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void manascore$aimBodyTowardsView(AbstractClientPlayer entity, float entityYaw, float partialTicks,
                                              PoseStack poseStack, MultiBufferSource bufferSource, int light,
                                              CallbackInfo ci) {
        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(entity);
        if (animation == null || !animation.aimBody) return;
        entity.yBodyRot = entity.yHeadRot;
        entity.yBodyRotO = entity.yHeadRotO;
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
