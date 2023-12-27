package com.github.manasmods.testmod.client;

import com.github.manasmods.testmod.storage.StorageTest;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class TestModClient {
    public static void init() {
        KeybindingTest.init();
        storageTestInit();
    }

    public static void storageTestInit() {
        ClientChatEvent.SEND.register((message, component) -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) StorageTest.printTestStorage(player);
            return EventResult.pass();
        });
    }
}
