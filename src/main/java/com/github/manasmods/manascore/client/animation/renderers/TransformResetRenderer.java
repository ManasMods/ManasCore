package com.github.manasmods.manascore.client.animation.renderers;

import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Resets changes made by the latest {@link TransformingRotatingRenderer}
 * Renderer ID: transformReset
 */
public class TransformResetRenderer implements IAnimationRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation) {
        poseStack.popPose();
    }
}
