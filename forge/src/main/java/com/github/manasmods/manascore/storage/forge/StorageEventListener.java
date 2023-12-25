package com.github.manasmods.manascore.storage.forge;

import com.github.manasmods.manascore.storage.StorageManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
