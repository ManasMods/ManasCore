package com.github.manasmods.testmod.storage;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.storage.Storage;
import com.github.manasmods.manascore.api.storage.StorageEvents;
import com.github.manasmods.manascore.storage.StorageManager.StorageKey;
import com.github.manasmods.testmod.TestMod;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StorageTest {
    private static StorageKey<TestStorage> KEY = null;

    public static void init() {
        StorageEvents.REGISTER_ENTITY_STORAGE.register(registry -> {
            KEY = registry.register(new ResourceLocation(TestMod.MOD_ID, "test_storage"), TestStorage.class, entity -> entity instanceof Player, target -> new TestStorage());
            ManasCore.Logger.info("Registered storage key: {}", KEY.id());
        });

        PlayerEvent.DROP_ITEM.register((player, entity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.manasCore$getStorageOptional(KEY).ifPresent(storage -> {
                    storage.dropCount++;
                    storage.markDirty();
                });
            }

            return EventResult.pass();
        });

        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            ManasCore.Logger.info("Drop count on {}: {} ", level.isClientSide ? "client" : "server", player.manasCore$getStorage(KEY).dropCount);
            return EventResult.pass();
        });

        ClientChatEvent.SEND.register((message, component) -> {
            Level level = Minecraft.getInstance().level;
            if(level == null) return EventResult.pass();
            Player player = Minecraft.getInstance().player;
            if(player == null) return EventResult.pass();
            ManasCore.Logger.info("Drop count on {}: {} ", level.isClientSide ? "client" : "server", player.manasCore$getStorage(KEY).dropCount);
            return EventResult.pass();
        });
    }

    public static class TestStorage extends Storage {
        private int dropCount;


        @Override
        public void save(CompoundTag data) {
            data.putInt("dropCount", dropCount);
        }

        @Override
        public void load(CompoundTag data) {
            this.dropCount = data.getInt("dropCount");
        }
    }
}
