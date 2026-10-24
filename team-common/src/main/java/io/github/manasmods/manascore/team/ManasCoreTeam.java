/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
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

/**
 * Entry point of the team module, called by the loader specific entry classes.
 */
public final class ManasCoreTeam {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Team");

    private ManasCoreTeam() {
    }

    /**
     * Used to register the config, network, registry, storage and built-in listeners of the team module.
     * Fires {@link TeamEvents#POST_INIT} when everything is set up.
     */
    public static void init() {
        ConfigRegistry.registerConfig(new TeamConfig());
        ManasTeamNetwork.init();
        TeamRegistry.init();
        TeamStorage.init();
        TeamManager.init();
        TeamEvents.POST_INIT.invoker().run();
    }
}
