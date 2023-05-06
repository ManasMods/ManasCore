package com.github.manasmods.manascore.client.animation.renderers;


import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Transforming rotating renderer is a simple renderer affecting the location of the render output by moving and rotating the model.
 * Be sure to use a {@link TransformResetRenderer} at the end of the chain for each transform renderer.
 *
 * Renderer ID: transformingRenderer
 *
 * The Transforming rotating renderer expects 5 arguments respectively
 * - the x component of the transformation as a double with the name "transformX"
 * - the y component of the transformation as a double with the name "transformY"
 * - the z component of the transformation as a double with the name "transformZ"
 * - the raw component of the rotation as a float with the name "rotationYaw"
 * - the pitch compoennt of the rotation as a float with the name "rotationPitch"
 *
 * Each argument is optional and will be set to 0 if empty.
 */
public class TransformingRotatingRenderer implements IAnimationRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation) {
        //Render empty method
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation, double transformX, double transformY, double transformZ, float rotationYaw, float rotationPitch) {
        poseStack.pushPose();

        poseStack.translate(transformX, transformY, transformZ);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationYaw));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationPitch));
    }
}
