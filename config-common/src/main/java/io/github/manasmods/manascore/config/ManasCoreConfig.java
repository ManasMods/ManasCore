package io.github.manasmods.manascore.config;

import com.electronwill.nightconfig.core.Config;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.manasmods.manascore.config.imp.network.ManasConfigNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManasCoreConfig {
    public static final Logger LOG = LoggerFactory.getLogger("ManasCore - Config");
    public static void init() {
        Config.setInsertionOrderPreserved(true);
        ManasConfigNetwork.init();
        PlayerEvent.PLAYER_JOIN.register(player -> ManasConfigNetwork.syncToClients());
    }
}
