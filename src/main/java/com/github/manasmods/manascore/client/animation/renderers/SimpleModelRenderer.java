package com.github.manasmods.manascore.client.animation.renderers;

import com.github.manasmods.manascore.api.client.animation.renderer.IAnimationRenderer;
import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SimpleModelRenderer implements IAnimationRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation) {

    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation, Model model, String texture) {
        RenderType type = RenderType.entitySolid(new ResourceLocation(texture));

        model.renderToBuffer(poseStack, bufferIn.getBuffer(type), 0, 0, 0, 0, 0, 0);
    }
}
