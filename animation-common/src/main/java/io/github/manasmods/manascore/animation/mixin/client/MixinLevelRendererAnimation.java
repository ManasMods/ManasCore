/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import io.github.manasmods.manascore.animation.mixin.accessor.AccessorCamera;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks the world render for the animation module and makes the {@link Camera} report as detached while the local player shows a first-person animation.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererAnimation {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();

    /**
     * Marks the world render, so the animation module can tell a player drawn into the level from one drawn into a GUI widget or a HUD overlay.
     * Those run after this method returns, in {@code Gui#render} and screen rendering, and must not get the first-person treatment.
     * <p>
     * The mark is cleared at {@code RETURN}. A throw inside {@code renderLevel} skips that and leaves it set, but the frame has already ended then.
     *
     * @see PlayerAnimationAPI#renderingLevel
     */
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void markRenderingLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        PlayerAnimationAPI.renderingLevel = true;
    }

    /**
     * Used to clear the mark set by {@link #markRenderingLevel}.
     */
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void clearRenderingLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        PlayerAnimationAPI.renderingLevel = false;
    }

    /**
     * Sets the camera detached right before the level renderer reads it, so the local player is drawn into the world while a first-person animation plays.
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
    private void fakeThirdPersonMode(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (camera.getEntity() instanceof Player player && PlayerAnimationAPI.state(player).firstPerson
                && manascore$mc.player == player) ((AccessorCamera) camera).setDetached(true);
    }

    /**
     * Used to clear the detached state again right after the level renderer has read it.
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z", shift = At.Shift.AFTER))
    private void resetThirdPerson(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        ((AccessorCamera) camera).setDetached(false);
    }
}
