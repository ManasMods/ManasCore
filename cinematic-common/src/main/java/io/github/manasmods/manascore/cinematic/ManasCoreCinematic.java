/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.manasmods.manascore.cinematic.api.CutsceneHelper;
import io.github.manasmods.manascore.cinematic.impl.client.ManasCoreCinematicClient;
import io.github.manasmods.manascore.cinematic.impl.network.ManasCinematicNetwork;

public class ManasCoreCinematic {
    public static void init() {
        ManasCinematicNetwork.init();
        CutsceneHelper.init();
        if (Platform.getEnvironment() == Env.CLIENT) ManasCoreCinematicClient.init();
    }
}
