package com.github.manasmods.manascore.client.animation.renderers;

import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class SimpleModelRenderer implements IAnimationRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation) {

    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation, Model model, RenderType type) {
        model.renderToBuffer(poseStack, bufferIn.getBuffer(type), 0, 0, 0, 0, 0, 0);
    }
}
