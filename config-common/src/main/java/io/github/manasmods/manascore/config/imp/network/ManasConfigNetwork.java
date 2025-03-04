/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.config.imp.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import io.github.manasmods.manascore.config.ConfigRegistry;
import io.github.manasmods.manascore.config.api.ManasConfig;
import io.github.manasmods.manascore.config.imp.network.s2c.SyncConfigToClientPayload;
import io.github.manasmods.manascore.network.api.util.NetworkUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ManasConfigNetwork {
    public static void init() {
        NetworkUtils.registerS2CPayload(SyncConfigToClientPayload.TYPE,
                SyncConfigToClientPayload.STREAM_CODEC, SyncConfigToClientPayload::handle);
    }

    /**
     * Syncs all registered server-side configurations to a specific player.
     * <p>
     * @param player The player to send the config data to.
     */
    public static void syncToClient(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, new SyncConfigToClientPayload(ConfigRegistry.getConfigSyncData()));
    }

    /**
     * Syncs a specific server-side configuration to a specific player.
     * <p>
     * @param player The player to send the config data to.
     * @param config The specific configuration class to sync.
     */
    public static void syncToClient(ServerPlayer player, Class<? extends ManasConfig> config) {
        NetworkManager.sendToPlayer(player, new SyncConfigToClientPayload(ConfigRegistry.getConfigSyncData(config)));
    }

    /**
     * Syncs all registered server-side configurations to all connected clients.
     * <p>
     * If called when no players are online, this method will do nothing.
     */
    public static void syncToClients() {
        MinecraftServer server = GameInstance.getServer();
        if (server == null) throw new RuntimeException("Failed find the Server.");
        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), new SyncConfigToClientPayload(ConfigRegistry.getConfigSyncData()));
    }

    /**
     * Syncs a specific server-side configuration to all connected clients.
     * <p>
     * @param config The specific configuration class to sync.
     */
    public static void syncToClients(Class<? extends ManasConfig> config) {
        MinecraftServer server = GameInstance.getServer();
        if (server == null) throw new RuntimeException("Failed find the Server.");
        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), new SyncConfigToClientPayload(ConfigRegistry.getConfigSyncData(config)));
    }
}
