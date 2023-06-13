package com.github.manasmods.manascore.client.animation;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class LevelLastAnimationRenderQueue extends AnimationRenderQueue {

    @SubscribeEvent
    public void onLevelLastRenderEvent(RenderLevelStageEvent e) {
        if(e.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
            this.render(e.getPoseStack());
        }
    }


}
