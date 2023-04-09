package com.github.manasmods.manascore.api.client.animation.renderer;

import com.github.manasmods.manascore.api.util.SharedStorage;
import com.github.manasmods.manascore.client.animation.CompiledAnimation;
import com.github.manasmods.manascore.client.animation.CompiledAnimationPart;
import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IAnimationRenderer {

    /**
     * Calls renderer with no arguments, example implementation, implement methods based on arguments
     * @param poseStack
     * @param bufferIn
     * @param animation
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, RunningAnimation animation);

}
