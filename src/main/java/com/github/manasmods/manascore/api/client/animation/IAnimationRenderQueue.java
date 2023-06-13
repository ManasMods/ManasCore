package com.github.manasmods.manascore.api.client.animation;

import com.github.manasmods.manascore.client.animation.RunningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;

public interface IAnimationRenderQueue {

    /**
     * Executes the animation, this will call the animation per render tick and update it at the end of every tick
     * @param animation the animation to run
     */
    public void run(RunningAnimation animation);

    public RunningAnimation getById(int id);

    public void stopAnimation(int id);
    public void stopAll();

    /**
     * Called from a render thread to render the animations
     * @param stack the pose stack from the LevelRenderer or render event
     */
    public void render(PoseStack stack);

    /**
     * Updates all animations on the queue, use this on post tick to update variables.
     */
    public void update();
}
