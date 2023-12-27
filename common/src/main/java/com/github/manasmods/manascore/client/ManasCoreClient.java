package com.github.manasmods.manascore.client;

import com.github.manasmods.manascore.client.keybinding.KeybindingManager;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;

public class ManasCoreClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> KeybindingManager.init());
        // Copy storage from old player to new player
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((oldPlayer, newPlayer) -> newPlayer.manasCore$setCombinedStorage(oldPlayer.manasCore$getCombinedStorage()));
    }
}
