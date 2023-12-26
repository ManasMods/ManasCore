package com.github.manasmods.testmod.storage;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.github.manasmods.testmod.TestMod;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class StorageTest {
    private static StorageKey<TestStorage> KEY = null;

    public static void init() {
        // Register storage
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {
            KEY = registry.register(new ResourceLocation(TestMod.MOD_ID, "test_storage"), TestStorage.class, entity -> entity instanceof Player, target -> new TestStorage());
            ManasCore.Logger.info("Registered storage key: {}", KEY.id());
        });
        // Register event listeners that change the storage
        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.manasCore$getStorageOptional(KEY).ifPresent(storage -> {
                    storage.dropCount++;
                    storage.markDirty();
                });
            }

            return EventResult.pass();
        });
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.manasCore$getStorageOptional(KEY).ifPresent(storage -> {
                    storage.deathCount++;
                    storage.markDirty();
                });
            }

            return EventResult.pass();
        });
        // Register event listeners that print the storage on client side and server side
        ClientChatEvent.SEND.register((message, component) -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) printTestStorage(player);
            return EventResult.pass();
        });
        ChatEvent.RECEIVED.register((player, component) -> {
            if (player != null) printTestStorage(player);
            return EventResult.pass();
        });
    }

    // Utility method to print the storage
    private static void printTestStorage(Player player) {
        boolean isClientSide = player.level().isClientSide();
        ManasCore.Logger.info("Storage of {} on {}:\n{} ", player.getId(), isClientSide ? "client" : "server", player.manasCore$getStorage(KEY));
    }

    // Storage implementation
    public static class TestStorage extends Storage {
        private int dropCount = 0;
        private int deathCount = 0;


        @Override
        public void save(CompoundTag data) {
            data.putInt("dropCount", dropCount);
            data.putInt("deathCount", deathCount);
        }

        @Override
        public void load(CompoundTag data) {
            this.dropCount = data.getInt("dropCount");
            this.deathCount = data.getInt("deathCount");
        }

        @Override
        public String toString() {
            return String.format("TestStorage{\n%s\n%s\n}", "dropCount=" + dropCount, "deathCount=" + deathCount);
        }
    }
}
