/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.keybind;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

public class ManasCoreKeybind {
    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            ManasCoreKeybindClient.init();
        }
    }
}
