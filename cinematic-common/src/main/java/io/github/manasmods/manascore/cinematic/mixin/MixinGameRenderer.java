/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.mixin;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void manascore$hideHandInCutscene(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (CutsceneClientHandler.isPlaying()) ci.cancel();
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void manascore$hideOutlineInCutscene(CallbackInfoReturnable<Boolean> cir) {
        if (CutsceneClientHandler.isPlaying()) cir.setReturnValue(false);
    }
}
