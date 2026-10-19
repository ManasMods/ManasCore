/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.cinematic.mixin;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void manascore$blockCutsceneMovement(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
        if (!CutsceneClientHandler.isPlaying()) return;
        KeyboardInput input = (KeyboardInput) (Object) this;
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }
}
