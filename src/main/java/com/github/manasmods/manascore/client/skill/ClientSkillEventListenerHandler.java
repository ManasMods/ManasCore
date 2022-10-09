package com.github.manasmods.manascore.client.skill;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.listener.KeyInputListener;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
class ClientSkillEventListenerHandler {
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.KeyInputEvent e) {
        if (mc.player == null) return;
        if (mc.level == null) return;
        for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(mc.player).getLearnedSkills()) {
            if (instance.getSkill() instanceof KeyInputListener listener) {
                listener.onKeyInput(instance, e);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderEntity(final EntityViewRenderEvent.CameraSetup e){

    }
}
