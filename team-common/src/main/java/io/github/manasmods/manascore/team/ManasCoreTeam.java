/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.team;

import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.team.api.TeamConfig;
import io.github.manasmods.manascore.team.api.template.TeamEvents;
import io.github.manasmods.manascore.team.impl.TeamManager;
import io.github.manasmods.manascore.team.impl.TeamRegistry;
import io.github.manasmods.manascore.team.impl.TeamStorage;
import io.github.manasmods.manascore.team.impl.network.ManasTeamNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManasCoreTeam {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Team");

    private ManasCoreTeam() {
    }

    public static void init() {
        ConfigRegistry.registerConfig(new TeamConfig());
        ManasTeamNetwork.init();
        TeamRegistry.init();
        TeamStorage.init();
        TeamManager.init();
        TeamEvents.POST_INIT.invoker().run();
    }
}
