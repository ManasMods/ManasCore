/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.storage.neoforge;

import io.github.manasmods.manascore.storage.ManasCoreStorage;
import io.github.manasmods.manascore.storage.ModuleConstants;
import io.github.manasmods.manascore.storage.impl.StorageManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

@Mod(ModuleConstants.MOD_ID)
public final class ManasCoreStorageNeoForge {
    public ManasCoreStorageNeoForge() {
        ManasCoreStorage.init();
        var neoEventBus = NeoForge.EVENT_BUS;
        neoEventBus.addListener(ManasCoreStorageNeoForge::onPlayerStartTrackingEntity);
        neoEventBus.addListener(ManasCoreStorageNeoForge::onPlayerStartTrackingChunk);
    }

    private static void onPlayerStartTrackingEntity(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StorageManager.syncTarget(event.getTarget(), player);
        }
    }

    private static void onPlayerStartTrackingChunk(ChunkWatchEvent.Sent e) {
        StorageManager.syncTarget(e.getChunk(), e.getPlayer());
    }
}
