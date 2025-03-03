package io.github.manasmods.manascore.config;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.config.imp.network.ManasConfigNetwork;
import io.github.manasmods.manascore.config.imp.network.s2c.SyncConfigToClientPayload;

public class ManasCoreConfig {
    public static void init() {
        ManasConfigNetwork.init();
        PlayerEvent.PLAYER_JOIN.register(player ->
                NetworkManager.sendToPlayer(player, new SyncConfigToClientPayload(ConfigRegistry.getConfigSyncData())));
    }
}
