/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybinding;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.manasmods.manascore.keybinding.api.KeybindingManager;

public class ManasCoreKeybindingClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> KeybindingManager.init());
    }
}
