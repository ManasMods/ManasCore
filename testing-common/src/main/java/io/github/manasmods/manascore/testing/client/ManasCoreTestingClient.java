/*
 * Copyright (c) 2024. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.client;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.manasmods.manascore.testing.module.InventoryTabsTest;
import io.github.manasmods.manascore.testing.module.StorageModuleTest;
import net.minecraft.client.Minecraft;

public class ManasCoreTestingClient {
    public static void init() {
        KeybindingTest.init();
        ClientChatEvent.RECEIVED.register((type, message) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) StorageModuleTest.printTestStorage(player);
            return CompoundEventResult.pass();
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> InventoryTabsTest.init(19));
    }
}
