/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.api;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.cinematic.impl.network.s2c.CutscenePayload;
import io.github.manasmods.manascore.skill.api.EntityEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CutsceneHelper {
    private static final Map<ResourceLocation, Cutscene> CUTSCENES = new HashMap<>();
    private static final Map<UUID, Long> WATCHERS = new HashMap<>();
    private static final Map<UUID, Long> LOADING = new HashMap<>();

    public static void init() {
        EntityEvents.LIVING_PRE_DAMAGED.register((entity, source, amount) ->
                isWatching(entity) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? EventResult.interruptFalse() : EventResult.pass());
        EntityEvents.LIVING_CHANGE_TARGET.register((entity, target) ->
                target.isPresent() && isWatching(target.get()) ? EventResult.interruptFalse() : EventResult.pass());
        // Cleared by PlayerLoadedPacket when the client's loading screen closes, 600 ticks matches vanilla's 30s screen timeout
        PlayerEvent.CHANGE_DIMENSION.register((player, oldDimension, newDimension) -> LOADING.put(player.getUUID(), player.serverLevel().getGameTime() + 600));
        PlayerEvent.PLAYER_QUIT.register(player -> {
            WATCHERS.remove(player.getUUID());
            LOADING.remove(player.getUUID());
        });
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            WATCHERS.clear();
            LOADING.clear();
        });
    }

    public static Cutscene register(Cutscene cutscene) {
        CUTSCENES.put(cutscene.getId(), cutscene);
        return cutscene;
    }

    public static @Nullable Cutscene getById(ResourceLocation id) {
        return CUTSCENES.get(id);
    }

    public static void play(Cutscene cutscene, List<ServerPlayer> players, Vec3 anchor, float yaw) {
        for (ServerPlayer player : players) {
            WATCHERS.put(player.getUUID(), player.serverLevel().getGameTime() + cutscene.getDuration());
            NetworkManager.sendToPlayer(player, new CutscenePayload(cutscene.getId(), anchor, yaw));
        }
    }

    public static boolean isWatching(Entity entity) {
        if (WATCHERS.isEmpty() || entity.level().isClientSide()) return false;
        Long end = WATCHERS.get(entity.getUUID());
        if (end == null) return false;
        if (entity.level().getGameTime() < end) return true;
        WATCHERS.remove(entity.getUUID());
        return false;
    }

    public static boolean isLoading(ServerPlayer player) {
        if (LOADING.isEmpty()) return false;
        Long end = LOADING.get(player.getUUID());
        if (end == null) return false;
        if (player.serverLevel().getGameTime() < end) return true;
        LOADING.remove(player.getUUID());
        return false;
    }

    public static void setLoaded(ServerPlayer player) {
        LOADING.remove(player.getUUID());
    }
}
