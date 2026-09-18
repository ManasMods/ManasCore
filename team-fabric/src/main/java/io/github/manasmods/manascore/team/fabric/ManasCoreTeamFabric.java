/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team.fabric;

import io.github.manasmods.manascore.team.ManasCoreTeam;
import net.fabricmc.api.ModInitializer;

public class ManasCoreTeamFabric implements ModInitializer {
    public void onInitialize() {
        ManasCoreTeam.init();
    }
}
