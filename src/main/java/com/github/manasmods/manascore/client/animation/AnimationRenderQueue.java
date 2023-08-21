package com.github.manasmods.manascore.client.animation;

import com.github.manasmods.manascore.api.client.animation.IAnimationRenderQueue;
import com.github.manasmods.manascore.core.MinecraftAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;

public class AnimationRenderQueue implements IAnimationRenderQueue {

    //I don't think there will be more than 2^32 animations in one session, if yes, they will likely be killed by that point.
    private int ANIMATION_RENDER_ID_GEN = 0;

    /**
     * Animations saved by handle id - this id is uniquely generated once the Animation is created
     */
    private HashMap<Integer, RunningAnimation> runningAnimations;


    /**
     * Runs the specified animation.
     *
     * Remember the initialization procedure
     * - RunningAnimation#create
     * - Set all parameters required for first run
     * - Invoke this method
     *
     * If the parameters are not set before running this method, the animation may crash the game.
     *
     * @param animation the animation to run
     */
    public void run(RunningAnimation animation) {
        int id = ++ANIMATION_RENDER_ID_GEN;
        this.runningAnimations.put(id, animation);

        animation.setId(id);

        //Update data before rendering for the first time.
        animation.postTick();
    }

    @Override
    public RunningAnimation getById(int id) {
        return this.runningAnimations.get(id);
    }

    @Override
    public void stopAnimation(int id) {
        if(this.runningAnimations.containsKey(id))
            this.runningAnimations.remove(id);
    }

    @Override
    public void stopAll() {
        this.runningAnimations.clear();
    }

    public void render(PoseStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftAccessor accessor = (MinecraftAccessor) minecraft;

        for (RunningAnimation animation : runningAnimations.values()) {
            animation.renderAnimation(stack, accessor.getRenderBuffers().bufferSource());
        }
    }


    public void update() {
        runningAnimations.values().forEach(RunningAnimation::postTick);
    }

}
