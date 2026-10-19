/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.mixin;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void manascore$blockCutsceneTurn(double movementTime, CallbackInfo ci) {
        if (CutsceneClientHandler.isPlaying()) ci.cancel();
    }
}
