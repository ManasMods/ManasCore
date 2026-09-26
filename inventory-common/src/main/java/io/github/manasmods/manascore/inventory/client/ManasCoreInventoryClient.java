/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.inventory.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.manasmods.manascore.inventory.InventoryTabRegistry;
import io.github.manasmods.manascore.inventory.VanillaInventoryTab;

public class ManasCoreInventoryClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
            InventoryTabRegistry.register(new VanillaInventoryTab());
        });
    }
}
