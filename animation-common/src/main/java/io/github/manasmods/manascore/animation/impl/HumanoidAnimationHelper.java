/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.impl;

import io.github.manasmods.manascore.animation.api.ConditionalAnimations;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Map;

public class HumanoidAnimationHelper {
    /**
     * Resets the model to the vanilla pivots before vanilla {@code setupAnim} runs, and clears the attack swing or the crouch when the active animation suppresses them.
     * Does nothing inside {@link PlayerAnimationAPI#withoutAnimations}.
     */
    public static void prepare(HumanoidModel<?> model, LivingEntity entity) {
        if (PlayerAnimationAPI.isSuppressed()) return;
        // Reset model pose
        resetModelPose(model);

        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(entity);
        if (animation == null) return;
        // Suppress attack swing and crouch
        boolean armsOrTorso = isReplacingVanillaPose(animation, "left_arm")
                || isReplacingVanillaPose(animation, "right_arm")
                || isReplacingVanillaPose(animation, "torso");
        if (animation.suppressAttack != null ? animation.suppressAttack : armsOrTorso) model.attackTime = 0;
        if (animation.suppressCrouch) model.crouching = false;
    }

    /**
     * Resets the pivots, rotations and scales that animations can change back to their vanilla values.
     */
    private static void resetModelPose(HumanoidModel<?> model) {
        // Leg width differs per model, player legs sit at 1.9 and skeleton legs at 2
        model.leftLeg.setPos(model.leftLeg.getInitialPose().x, 12.0F, 0.0F);
        model.rightLeg.setPos(model.rightLeg.getInitialPose().x, 12.0F, 0.0F);
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

    /**
     * Determine if the animation replaces the vanilla pose of the bone, which is when it animates the bone without listing it as additive.
     */
    private static boolean isReplacingVanillaPose(PlayerAnimationAPI.PlayerAnimation animation, String boneName) {
        return animation.bones.get(boneName) != null && !animation.additiveBones.contains(boneName);
    }

    /**
     * Poses the model from the animation of the entity and advances its playback.
     * <p>
     * Progress runs on the entity tick count plus the partial tick of the frame, not on {@code ageInTicks}.
     * The same entity is often posed several times a frame (world, first-person hands, inventory preview, stray clothing layer) with different partial ticks,
     * so the caller's {@code ageInTicks} would make the progress jitter. With one clock every call of a frame lands on the same progress,
     * and whichever call comes first advances it.
     */
    public static void applyAnimation(HumanoidModel<?> model, LivingEntity entity, float ageInTicks, float netHeadYaw, float headPitch) {
        if (PlayerAnimationAPI.isSuppressed()) return;
        if (ageInTicks <= 0) return;

        PlayerAnimationAPI.PlayerAnimationState data = PlayerAnimationAPI.state(entity);
        String playingAnimation = data.currentAnimation;
        boolean overrideAnimation = data.override;
        // Apply pending reset
        if (data.reset) {
            data.reset = false;
            data.lastAnimationProgress = 0f;
            data.playedSounds.clear();
            PlayerAnimationAPI.active_animations.remove(entity);
        }

        // Fall back to conditional animation
        if (playingAnimation.isEmpty()) {
            ConditionalAnimations.ConditionalAnimation conditional = ConditionalAnimations.evaluate(entity);
            String conditionalKey = conditional == null ? "" : conditional.key();
            if (!conditionalKey.equals(data.currentConditional)) {
                data.currentConditional = conditionalKey;
                data.hasProgress = false;
                data.lastAnimationProgress = 0f;
                data.playedSounds.clear();
                data.firstPerson = conditional != null && conditional.firstPerson();
                PlayerAnimationAPI.active_animations.remove(entity);
            }
            if (conditional == null) return;
            playingAnimation = conditionalKey;
        } else if (!data.currentConditional.isEmpty()) {
            data.currentConditional = "";
        }

        // Square the body up with the head in first person
        boolean firstPerson = PlayerAnimationAPI.isFirstPersonActive(entity) && isRenderingOwnView(entity);
        if (firstPerson) {
            entity.yBodyRotO = entity.yHeadRotO;
            entity.yBodyRot = entity.yHeadRot;
        }

        // Restart on override
        if (overrideAnimation) {
            data.override = false;
            data.hasProgress = false;
            data.lastAnimationProgress = 0f;
            data.playedSounds.clear();
            firstPerson = PlayerAnimationAPI.isFirstPersonActive(entity) && isRenderingOwnView(entity);
            PlayerAnimationAPI.active_animations.remove(entity);
        }

        // Resolve animation
        PlayerAnimationAPI.PlayerAnimation animation = PlayerAnimationAPI.active_animations.get(entity);
        if (animation == null) {
            animation = PlayerAnimationAPI.animations.get(playingAnimation);
            if (animation == null) {
                PlayerAnimationAPI.LOG.info("Attempted to play null animation {}, did animations fail to load?", playingAnimation);
                return;
            }
            PlayerAnimationAPI.active_animations.put(entity, animation);
        }

        // Update progress
        float time = entity.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float animationProgress;
        float lastAnimationProgress = data.lastAnimationProgress;
        if (!data.hasProgress) {
            animationProgress = 0f;
            data.hasProgress = true;
            data.progress = animationProgress;
            data.lastTickTime = time;
            data.lastAnimationProgress = 0f;

        } else {
            animationProgress = data.progress;
            float lastTickTime = data.lastTickTime;
            float deltaTime = Math.max(0f, (time - lastTickTime) / 20f);
            animationProgress += deltaTime;
            data.progress = animationProgress;
            data.lastTickTime = time;
            // End of animation
            if (animationProgress >= animation.length) {
                if (!animation.hold_on_last_frame && !animation.loop) {

                    // Play next animation
                    if (!data.nextAnimation.isEmpty()
                            && PlayerAnimationAPI.canPlay(data.nextAnimation, data.currentAnimation)) {
                        data.currentAnimation = data.nextAnimation;
                        data.nextAnimation = "";
                        data.hasProgress = false;
                        data.lastAnimationProgress = 0f;
                        data.playedSounds.clear();
                        PlayerAnimationAPI.active_animations.remove(entity);
                        animationProgress = animation.length;
                    } else {
                        // Reset animation
                        data.currentAnimation = "";
                        data.nextAnimation = "";
                        data.hasProgress = false;
                        data.lastAnimationProgress = 0f;
                        data.playedSounds.clear();
                        data.reset = true;
                        data.firstPerson = false;
                        PlayerAnimationAPI.active_animations.remove(entity);
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

        // Play sound effects
        if (!animation.soundEffects.isEmpty()) {
            for (Map.Entry<Float, String> soundEntry : animation.soundEffects.entrySet()) {
                float soundTime = soundEntry.getKey();
                String soundId = soundEntry.getValue();
                if (data.playedSounds.contains(soundTime)) continue;
                boolean shouldPlay;
                // The progress wraps around when the animation looped
                if (lastAnimationProgress <= animationProgress) shouldPlay = lastAnimationProgress <= soundTime && animationProgress >= soundTime;
                else shouldPlay = lastAnimationProgress <= soundTime || animationProgress >= soundTime;
                if (shouldPlay && entity.level() instanceof ClientLevel clientLevel) {
                    clientLevel.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
                    data.playedSounds.add(soundTime);
                }
            }
            data.lastAnimationProgress = animationProgress;
        }

        // Apply bones, third person animations are skipped for the own view
        if (!PlayerAnimationAPI.isFirstPersonActive(entity) && isRenderingOwnView(entity)) return;
        for (Map.Entry<String, PlayerAnimationAPI.PlayerBone> entry : animation.bones.entrySet()) {
            String boneName = entry.getKey();
            PlayerAnimationAPI.PlayerBone bone = entry.getValue();
            ModelPart modelPart = getModelPart(model, boneName);
            if (modelPart == null) continue;
            boolean firstPersonArms = firstPerson && (boneName.equals("right_arm") || boneName.equals("left_arm"));

            // Rotate bone
            Vec3 rotation = PlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress, entity);
            if (rotation != null) {
                if (animation.additiveBones.contains(boneName)) {
                    modelPart.xRot += (float) Math.toRadians(rotation.x);
                    modelPart.yRot += (float) Math.toRadians(rotation.y);
                    modelPart.zRot += (float) Math.toRadians(rotation.z);
                } else {
                    modelPart.xRot = (float) Math.toRadians(rotation.x);
                    modelPart.yRot = (float) Math.toRadians(rotation.y);
                    modelPart.zRot = (float) Math.toRadians(rotation.z);
                }
            }

            // Aim bone
            if (animation.aimBones.contains(boneName) && !firstPersonArms) {
                applyAim(modelPart, netHeadYaw, headPitch);
            }

            // Move bone
            Vec3 position = PlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress, entity);
            if (position != null) {
                modelPart.x += (float) position.x;
                modelPart.y -= (float) position.y;
                modelPart.z += (float) position.z;
            }

            // Scale bone
            Vec3 scale = PlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress, entity);
            if (scale != null) {
                modelPart.xScale = (float) scale.x;
                modelPart.yScale = (float) scale.y;
                modelPart.zScale = (float) scale.z;
            }

            // Follow the pitch of the view with first person arms
            if (firstPersonArms) {
                float frameBuffer = 0.09f;
                float timeLeft = animation.length - animationProgress;
                float fpWeight = 1.0f;
                boolean rightArm = boneName.equals("right_arm");
                // Fade out in the last frames of a non looping animation
                if (!animation.loop && timeLeft < frameBuffer) {
                    fpWeight = Math.max(0f, timeLeft / frameBuffer);
                }

                if (fpWeight > 0) {
                    float pitchRadians = (float) Math.toRadians(entity.getXRot());
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

        model.hat.copyFrom(model.head);
    }

    /**
     * Determine if this render is the local player seeing themselves from their own eyes, which is the one case where the body is the camera.
     * <p>
     * {@link PlayerAnimationAPI#renderingLevel} keeps GUI previews and HUD widgets out, because they draw the same player
     * from the same camera type and need the ordinary third-person treatment.
     */
    private static boolean isRenderingOwnView(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        return PlayerAnimationAPI.renderingLevel && mc.options.getCameraType().isFirstPerson() && entity == mc.player;
    }

    /**
     * Used to get the {@link ModelPart} of a bone name.
     * Returns null for bones that are not model parts, like {@code body}, {@code right_item} and {@code left_item}.
     */
    private static ModelPart getModelPart(HumanoidModel<?> model, String boneName) {
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

    /**
     * Swings an aim bone onto the look direction of the entity. The authored pose is carried rigidly, so the bone keeps its shape and pivots at its own origin (the shoulder, for an arm).
     * <p>
     * The view angles are composed as an <em>outer</em> rotation instead of being added into {@code xRot} and {@code yRot}.
     * {@link ModelPart#translateAndRotate} rebuilds the rotation as {@code Rz * Ry * Rx} with Z outermost, so an added angle would land inside the authored roll and tilt the whole aiming plane.
     * Vanilla can add on the head only because the base {@code yRot} and {@code zRot} of the head are zero.
     * <p>
     * {@code netHeadYaw} is head yaw minus body yaw, so it is the yaw the body has not covered yet.
     * With {@code manascore:aim_body} it collapses to zero and this contributes pitch alone, otherwise it closes the remaining gap.
     * Both terms are written on purpose, do not reduce this to pitch only.
     */
    private static void applyAim(ModelPart modelPart, float netHeadYaw, float headPitch) {
        // Compose the view rotation outside the authored pose
        float toRadians = (float) Math.PI / 180F;
        Quaternionf composed = new Quaternionf()
                .rotationYXZ(netHeadYaw * toRadians, headPitch * toRadians, 0f)
                .mul(new Quaternionf().rotationZYX(modelPart.zRot, modelPart.yRot, modelPart.xRot));
        // Convert back to euler angles
        float x = composed.x, y = composed.y, z = composed.z, w = composed.w;
        float sinY = Mth.clamp(-2f * (x * z - y * w), -1f, 1f);
        modelPart.yRot = (float) Math.asin(sinY);
        if (Math.abs(sinY) < 0.99999f) {
            modelPart.xRot = (float) Math.atan2(2f * (y * z + x * w), 1f - 2f * (x * x + y * y));
            modelPart.zRot = (float) Math.atan2(2f * (x * y + z * w), 1f - 2f * (y * y + z * z));
        } else {
            // Gimbal lock
            modelPart.xRot = (float) Math.atan2(-2f * (y * z - x * w), 1f - 2f * (x * x + z * z));
            modelPart.zRot = 0f;
        }
    }
}
