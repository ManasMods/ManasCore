/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybind;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.manasmods.manascore.keybind.api.KeybindingManager;

public class ManasCoreKeybindClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> KeybindingManager.init());
    }
}
