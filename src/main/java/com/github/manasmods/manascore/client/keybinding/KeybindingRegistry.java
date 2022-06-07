package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.ManasCore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeybindingRegistry {
    private static final ArrayList<ManasKeybinding> keybindings = new ArrayList<>();

    @SubscribeEvent
    public static void register(final FMLClientSetupEvent e) {
        keybindings.forEach(keybinding -> e.enqueueWork(() -> ClientRegistry.registerKeyBinding(keybinding)));
    }

    public static void register(ManasKeybinding... keybinding) {
        keybindings.addAll(Arrays.asList(keybinding));
    }

    public static void checkKeybindings(final InputEvent.KeyInputEvent e) {
        keybindings.forEach(keybinding -> {
            if (keybinding.isDown()) {
                keybinding.getAction().onPress();
                ManasCore.getLogger().debug("Pressed Keybinding {}", keybinding.getTranslatedKeyMessage().getString());
            }
        });
    }
}
