/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.client;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import io.github.manasmods.manascore.testing.module.InventoryTabsTest;
import io.github.manasmods.manascore.testing.module.StorageModuleTest;
import io.github.manasmods.manascore.testing.registry.RegistryTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.VillagerRenderer;

public class ManasCoreTestingClient {
    public static void init() {
        KeybindingTest.init();
        ClientChatEvent.RECEIVED.register((type, message) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) StorageModuleTest.printTestStorage(player);
            return CompoundEventResult.pass();
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> InventoryTabsTest.init(19));
        EntityRendererRegistry.register(RegistryTest.TEST_ENTITY_TYPE::value, VillagerRenderer::new);
    }
}
