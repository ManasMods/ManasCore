package com.github.manasmods.manascore.api.client.animation.renderer;

import com.github.manasmods.manascore.client.animation.CompiledAnimation;
import com.github.manasmods.manascore.client.animation.CompiledAnimationPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IAnimationRenderer {

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, CompiledAnimationPart animationPart);

}
