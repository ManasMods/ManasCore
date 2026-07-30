/*
 * Copyright (c) 2025-2026. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets an animation pose the held item independently of the arm holding it, through the reserved bone names
 * {@code right_item} and {@code left_item}.
 * <p>
 * Without this the item is welded to the arm: {@code ItemInHandLayer} calls {@code translateToHand}, which is
 * just {@code getArm(arm).translateAndRotate(poseStack)}, so the item inherits the arm's rotation, position and
 * scale and nothing else. That is usually what you want, but it makes a sword spinning in the hand, or an item
 * drifting away from the palm, impossible to author.
 * <p>
 * These bones are not {@link net.minecraft.client.model.geom.ModelPart}s, so they are invisible to
 * {@code MixinPlayerModel} - {@code manascore$getModelPart} returns null for them and the bone loop skips them.
 * They are applied here as pose stack transforms instead, which also means they compose on top of whatever the
 * arm is doing, including {@code aim_bones}.
 */
@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayerAnimation {

    @Inject(method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void manascore$animateHeldItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
                                           HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource,
                                           int light, CallbackInfo ci) {
        if (stack.isEmpty()) return;
        if (!(entity instanceof Player player)) return;
        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(player);
        if (animation == null) return;

        String boneName = arm == HumanoidArm.LEFT ? "left_item" : "right_item";
        PlayerAnimationAPI.PlayerBone bone = animation.bones.get(boneName);
        if (bone == null) return;

        float animationProgress = PlayerAnimationAPI.state(player).progress;
        Vec3 position = PlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress, player);
        if (position != null) poseStack.translate((float) position.x * 0.0625f, (float) -position.y * 0.0625f, (float) position.z * 0.0625f);

        Vec3 rotation = PlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress, player);
        if (rotation != null) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation.y));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) rotation.x));
        }

        Vec3 scale = PlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress, player);
        if (scale != null) poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }
}