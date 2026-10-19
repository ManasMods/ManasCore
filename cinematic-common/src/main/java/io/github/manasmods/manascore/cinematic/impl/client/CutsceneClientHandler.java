/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.impl.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.manasmods.manascore.cinematic.api.Cutscene;
import io.github.manasmods.manascore.cinematic.api.client.CinematicClientEvents;
import io.github.manasmods.manascore.cinematic.impl.network.c2s.PlayerLoadedPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class CutsceneClientHandler {
    private static Cutscene cutscene;
    private static Vec3 anchor = Vec3.ZERO;
    private static float yaw;
    private static int elapsed;
    private static int fadeOut;
    private static boolean loading;

    public static void init() {
        ClientTickEvent.CLIENT_PRE.register(minecraft -> {
            boolean receiving = minecraft.screen instanceof ReceivingLevelScreen;
            if (loading && !receiving && minecraft.getConnection() != null && NetworkManager.canServerReceive(PlayerLoadedPacket.TYPE)) NetworkManager.sendToServer(new PlayerLoadedPacket());
            loading = receiving;
        });

        ClientTickEvent.CLIENT_PRE.register(minecraft -> {
            if (minecraft.isPaused()) return;
            if (fadeOut > 0) fadeOut--;
            if (cutscene == null) return;

            if (minecraft.options.keyJump.consumeClick() && elapsed > 20) elapsed = Math.max(elapsed, cutscene.getDuration() - 12);
            KeyMapping.releaseAll();
            if (++elapsed < cutscene.getDuration()) return;
            cutscene = null;
            fadeOut = 10;
        });

        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            cutscene = null;
            fadeOut = 0;
            loading = false;
        });
    }

    public static void start(Cutscene instance, Vec3 position, float facing) {
        if (CinematicClientEvents.CUTSCENE_START.invoker().start(instance).isFalse()) return;
        cutscene = instance;
        anchor = position;
        yaw = facing;
        elapsed = 0;
        fadeOut = 0;
    }

    public static boolean isPlaying() {
        return cutscene != null;
    }

    public static boolean isShowing() {
        return cutscene != null || fadeOut > 0;
    }

    public static @Nullable Cutscene getCutscene() {
        return cutscene;
    }

    public static float getTime(float partialTick) {
        return cutscene == null ? 0 : Math.min(elapsed + partialTick, cutscene.getDuration());
    }

    public static float getFadeOut(float partialTick) {
        return Math.max(fadeOut - partialTick, 0) / 10F;
    }

    public static Vec3 getCameraTarget(float partialTick) {
        return cutscene.getTargetAt(getTime(partialTick), anchor, yaw);
    }

    public static Vec3 getCameraPosition(BlockGetter level, Vec3 target, float partialTick) {
        Vec3 position = cutscene.getPositionAt(getTime(partialTick), anchor, yaw);
        BlockHitResult hit = level.clip(new ClipContext(target, position, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (hit.getType() == HitResult.Type.MISS) return position;
        return hit.getLocation().add(target.subtract(hit.getLocation()).normalize().scale(0.3D));
    }

    public static float getShake(int speed, int offset) {
        return CinematicClientEvents.CAMERA_SHAKE.invoker().getShake(speed, offset);
    }
}
