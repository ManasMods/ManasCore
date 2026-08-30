/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.race;

import io.github.manasmods.manascore.race.api.RaceEvents;
import io.github.manasmods.manascore.race.impl.RaceRegistry;
import io.github.manasmods.manascore.race.impl.RaceStorage;
import io.github.manasmods.manascore.race.impl.network.ManasRaceNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManasCoreRace {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Race");

    public static void init() {
        RaceRegistry.init();
        RaceStorage.init();
        ManasRaceNetwork.init();
        RaceEvents.POST_INIT.invoker().run();
    }
}
