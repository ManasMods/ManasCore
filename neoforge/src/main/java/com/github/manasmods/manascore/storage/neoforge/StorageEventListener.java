package com.github.manasmods.manascore.storage.neoforge;

import com.github.manasmods.manascore.storage.StorageManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class StorageEventListener {
    @SubscribeEvent
    static void onPlayerRespawn(final PlayerEvent.Clone e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            StorageManager.syncTarget(e.getOriginal(), player);
        }
    }

    @SubscribeEvent
    static void onPlayerTrack(final PlayerEvent.StartTracking e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            StorageManager.syncTarget(e.getTarget(), player);
        }
    }
}
