/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import io.github.manasmods.manascore.animation.api.ConditionalAnimations;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel<T extends LivingEntity> {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();

    @Inject(method = "setupAnim*", at = @At(value = "HEAD"))
    public void setupPivot(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        PlayerModel<T> model = (PlayerModel<T>) (Object) this;
        manascore$resetModelPose(model);
        if (!(entityIn instanceof Player player)) return;

        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(player);
        if (animation == null) return;
        if (animation.bones.get("left_arm") != null || animation.bones.get("torso") != null
                || animation.bones.get("right_arm") != null) model.attackTime = 0;
        model.crouching = false;
    }

    @Inject(method = "setupAnim*", at = @At(value = "TAIL"))
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (ageInTicks <= 0) return;
        PlayerModel<T> model = (PlayerModel<T>) (Object) this;
        if (!(entityIn instanceof Player player)) return;

        PlayerAnimationAPI.PlayerAnimationState data = PlayerAnimationAPI.state(player);
        String playingAnimation = data.currentAnimation;
        boolean overrideAnimation = data.override;
        if (data.reset) {
            data.reset = false;
            data.lastAnimationProgress = 0f;
            data.playedSounds.clear();
            PlayerAnimationAPI.active_animations.put(player, null);
        }

        // No packet-driven animation: fall back to a condition-driven ambient pose (client-evaluated, unsynced by design).
        if (playingAnimation.isEmpty()) {
            ConditionalAnimations.ConditionalAnimation conditional = ConditionalAnimations.evaluate(player);
            String conditionalKey = conditional == null ? "" : conditional.key();
            if (!conditionalKey.equals(data.currentConditional)) {
                data.currentConditional = conditionalKey;
                data.hasProgress = false;
                data.lastAnimationProgress = 0f;
                data.playedSounds.clear();
                data.firstPerson = conditional != null && conditional.firstPerson();
                PlayerAnimationAPI.active_animations.put(player, null);
            }
            if (conditional == null) return;
            playingAnimation = conditionalKey;
        } else if (!data.currentConditional.isEmpty()) {
            data.currentConditional = "";
        }

        boolean firstPerson = data.firstPerson && !PlayerAnimationAPI.renderingGuiEntity && manascore$mc.options.getCameraType().isFirstPerson() && player == manascore$mc.player && manascore$mc.screen == null;
        if (firstPerson) {
            player.yBodyRotO = player.yHeadRotO;
            player.yBodyRot = player.yHeadRot;
        }

        if (overrideAnimation) {
            data.override = false;
            data.hasProgress = false;
            data.lastAnimationProgress = 0f;
            data.playedSounds.clear();
            firstPerson = data.firstPerson && !PlayerAnimationAPI.renderingGuiEntity && manascore$mc.options.getCameraType().isFirstPerson() && player == manascore$mc.player && manascore$mc.screen == null;
            PlayerAnimationAPI.active_animations.put(player, null);
        }

        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(player);
        if (animation == null) {
            animation = PlayerAnimationAPI.animations.get(playingAnimation);
            if (animation == null) {
                PlayerAnimationAPI.LOG.info("Attempted to play null animation {}, did animations fail to load?", playingAnimation);
                return;
            }
            PlayerAnimationAPI.active_animations.put(player, animation);
        }

        float animationProgress;
        float lastAnimationProgress = data.lastAnimationProgress;
        if (!data.hasProgress) {
            animationProgress = 0f;
            data.hasProgress = true;
            data.progress = animationProgress;
            data.lastTickTime = ageInTicks;
            data.lastAnimationProgress = 0f;
        } else {

            animationProgress = data.progress;
            float lastTickTime = data.lastTickTime;
            float deltaTime = (ageInTicks - lastTickTime) / 20f;
            animationProgress += deltaTime;
            data.progress = animationProgress;
            data.lastTickTime = ageInTicks;
            if (animationProgress >= animation.length) {
                if (!animation.hold_on_last_frame && !animation.loop) {
                    if (!data.nextAnimation.isEmpty()) {
                        data.currentAnimation = data.nextAnimation;
                        data.nextAnimation = "";
                        data.hasProgress = false;
                        data.lastAnimationProgress = 0f;
                        data.playedSounds.clear();
                        PlayerAnimationAPI.active_animations.put(player, null);
                        animationProgress = animation.length;
                    } else {
                        data.currentAnimation = "";
                        data.hasProgress = false;
                        data.lastAnimationProgress = 0f;
                        data.playedSounds.clear();
                        data.reset = true;
                        data.firstPerson = false;
                        PlayerAnimationAPI.active_animations.put(player, null);
                        animationProgress = animation.length;
                    }
                } else if (animation.hold_on_last_frame) {
                    data.progress = animation.length;
                } else if (animation.loop) {
                    data.hasProgress = false;
                    data.lastAnimationProgress = 0f;
                    data.playedSounds.clear();
                }
            }
        }

        if (!animation.soundEffects.isEmpty()) {
            for (Map.Entry<Float, String> soundEntry : animation.soundEffects.entrySet()) {
                float soundTime = soundEntry.getKey();
                String soundId = soundEntry.getValue();
                if (data.playedSounds.contains(soundTime)) continue;
                boolean shouldPlay;
                if (lastAnimationProgress <= animationProgress) shouldPlay = lastAnimationProgress <= soundTime && animationProgress >= soundTime;
                else shouldPlay = lastAnimationProgress <= soundTime || animationProgress >= soundTime;
                if (shouldPlay && player.level() instanceof ClientLevel clientLevel) {
                    clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
                    data.playedSounds.add(soundTime);
                }
            }
            data.lastAnimationProgress = animationProgress;
        }

        if (!data.firstPerson && manascore$mc.options.getCameraType().isFirstPerson()
                && player == manascore$mc.player && manascore$mc.screen == null) return;
        for (Map.Entry<String, PlayerAnimationAPI.PlayerBone> entry : animation.bones.entrySet()) {
            String boneName = entry.getKey();
            PlayerAnimationAPI.PlayerBone bone = entry.getValue();
            ModelPart modelPart = manascore$getModelPart(model, boneName);
            if (modelPart == null) continue;

            Vec3 rotation = PlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress, player);
            if (rotation != null) {
                modelPart.xRot = (float) Math.toRadians(rotation.x);
                modelPart.yRot = (float) Math.toRadians(rotation.y);
                modelPart.zRot = (float) Math.toRadians(rotation.z);
            }

            Vec3 position = PlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress, player);
            if (position != null) {
                modelPart.x += (float) position.x;
                modelPart.y -= (float) position.y;
                modelPart.z += (float) position.z;
            }

            Vec3 scale = PlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress, player);
            if (scale != null) {
                modelPart.xScale = (float) scale.x;
                modelPart.yScale = (float) scale.y;
                modelPart.zScale = (float) scale.z;
            }

            boolean firstPersonArms = firstPerson && (boneName.equals("right_arm") || boneName.equals("left_arm"));
            if (firstPersonArms) {
                float frameBuffer = 0.09f;
                float timeLeft = animation.length - animationProgress;
                float fpWeight = 1.0f;
                boolean rightArm = boneName.equals("right_arm");
                if (!animation.loop && timeLeft < frameBuffer) {
                    fpWeight = Math.max(0f, timeLeft / frameBuffer);
                }

                if (fpWeight > 0) {
                    float pitchRadians = (float) Math.toRadians(player.getXRot());
                    modelPart.xRot += pitchRadians * fpWeight;
                    float yRotCorrection = pitchRadians * (rightArm ? -0.42f : 0.42f);
                    modelPart.yRot += yRotCorrection * fpWeight;
                    float zRotCorrection = pitchRadians * (rightArm ? -0.34f : 0.34f);
                    modelPart.zRot += zRotCorrection * fpWeight;
                    float originalY = modelPart.y;
                    float originalZ = modelPart.z;
                    float cosP = (float) Math.cos(pitchRadians);
                    float sinP = (float) Math.sin(pitchRadians);
                    float targetY = originalY * cosP - originalZ * sinP;
                    float targetZ = originalY * sinP + originalZ * cosP;
                    modelPart.y = originalY + (targetY - originalY) * fpWeight;
                    modelPart.z = originalZ + (targetZ - originalZ) * fpWeight;
                }
            }
        }

        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.jacket.copyFrom(model.body);
        model.hat.copyFrom(model.head);
    }

    @Unique
    private void manascore$resetModelPose(PlayerModel<T> model) {
        model.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        model.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        model.head.setPos(0.0F, 0.0F, 0.0F);
        model.rightArm.z = 0.0F;
        model.rightArm.x = -5.0F;
        model.leftArm.z = 0.0F;
        model.leftArm.x = 5.0F;
        model.body.xRot = 0.0F;
        model.rightLeg.z = 0.1F;
        model.leftLeg.z = 0.1F;
        model.rightLeg.y = 12.0F;
        model.leftLeg.y = 12.0F;
        model.head.y = 0.0F;
        model.head.zRot = 0f;
        model.body.y = 0.0F;
        model.body.x = 0f;
        model.body.z = 0f;
        model.body.yRot = 0;
        model.body.zRot = 0;
        model.head.xScale = ModelPart.DEFAULT_SCALE;
        model.head.yScale = ModelPart.DEFAULT_SCALE;
        model.head.zScale = ModelPart.DEFAULT_SCALE;
        model.body.xScale = ModelPart.DEFAULT_SCALE;
        model.body.yScale = ModelPart.DEFAULT_SCALE;
        model.body.zScale = ModelPart.DEFAULT_SCALE;
        model.rightArm.xScale = ModelPart.DEFAULT_SCALE;
        model.rightArm.yScale = ModelPart.DEFAULT_SCALE;
        model.rightArm.zScale = ModelPart.DEFAULT_SCALE;
        model.leftArm.xScale = ModelPart.DEFAULT_SCALE;
        model.leftArm.yScale = ModelPart.DEFAULT_SCALE;
        model.leftArm.zScale = ModelPart.DEFAULT_SCALE;
        model.rightLeg.xScale = ModelPart.DEFAULT_SCALE;
        model.rightLeg.yScale = ModelPart.DEFAULT_SCALE;
        model.rightLeg.zScale = ModelPart.DEFAULT_SCALE;
        model.leftLeg.xScale = ModelPart.DEFAULT_SCALE;
        model.leftLeg.yScale = ModelPart.DEFAULT_SCALE;
        model.leftLeg.zScale = ModelPart.DEFAULT_SCALE;
    }

    @Unique
    private ModelPart manascore$getModelPart(PlayerModel<T> model, String boneName) {
        return switch (boneName) {
            case "torso" -> model.body;
            case "head" -> model.head;
            case "right_arm" -> model.rightArm;
            case "left_arm" -> model.leftArm;
            case "right_leg" -> model.rightLeg;
            case "left_leg" -> model.leftLeg;
            default -> null;
        };
    }
}
