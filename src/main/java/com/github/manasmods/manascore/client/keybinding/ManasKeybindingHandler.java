package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ManasKeybindingHandler {
    @SubscribeEvent
    public static void onKeyInput(final InputEvent.KeyInputEvent e) {
        KeybindingRegistry.checkKeybindings(e);
    }
}
