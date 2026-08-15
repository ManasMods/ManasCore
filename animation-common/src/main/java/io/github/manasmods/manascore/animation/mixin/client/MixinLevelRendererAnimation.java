/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
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

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererAnimation {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();

    /**
     * Marks the world render, which is what tells the rest of the animation module that a player is being
     * drawn into the level and not into a GUI widget or a HUD overlay - both of those run after this method
     * returns, in {@code Gui#render} and screen rendering, and must not get the first-person treatment.
     * <p>
     * Cleared at {@code RETURN}, which a throw inside {@code renderLevel} would skip. That leaves the flag
     * stuck true, but a throw here has already ended the frame, so nothing renders to observe it.
     */
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void markRenderingLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        PlayerAnimationAPI.renderingLevel = true;
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void clearRenderingLevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        PlayerAnimationAPI.renderingLevel = false;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"))
    private void fakeThirdPersonMode(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (camera.getEntity() instanceof Player player && PlayerAnimationAPI.state(player).firstPerson
                && manascore$mc.player == player) ((AccessorCamera) camera).setDetached(true);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z", shift = At.Shift.AFTER))
    private void resetThirdPerson(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        ((AccessorCamera) camera).setDetached(false);
    }
}
