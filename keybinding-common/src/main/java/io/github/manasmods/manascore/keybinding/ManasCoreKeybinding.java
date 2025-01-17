/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybinding;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public class ManasCoreKeybinding {
    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            ManasCoreKeybindingClient.init();
        }
    }
}
