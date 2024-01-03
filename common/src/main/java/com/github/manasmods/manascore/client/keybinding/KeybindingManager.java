package com.github.manasmods.manascore.client.keybinding;

import com.github.manasmods.manascore.api.client.keybinding.ManasKeybinding;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

import java.util.ArrayList;

public class KeybindingManager {
    private static final ArrayList<ManasKeybinding> keybindings = new ArrayList<>();

    public static <T extends ManasKeybinding> void register(T... keybindings) {
        for (final ManasKeybinding keybinding : keybindings) {
            register(keybinding);
        }
    }

    public static <T extends ManasKeybinding> T register(T keybinding) {
        keybindings.add(keybinding);
        KeyMappingRegistry.register(keybinding);
        return keybinding;
    }

    public static void init() {
        // Execute Actions on press
        ClientTickEvent.CLIENT_POST.register(instance -> {
            for (final ManasKeybinding keybinding : keybindings) {
                if (keybinding.isDown()) {
                    keybinding.getAction().onPress();
                } else if (keybinding.getRelease() != null) {
                    keybinding.getRelease().run();
                }
            }
        });
    }
}
