/*
 * Copyright (c) 2026. ManasMods
 * GNU Lesser General Public License 3
 */

package io.github.manasmods.manascore.cinematic.mixin;

import io.github.manasmods.manascore.cinematic.impl.client.CutsceneClientHandler;
import io.github.manasmods.manascore.cinematic.impl.client.CutsceneOverlay;
import io.github.manasmods.manascore.cinematic.impl.client.DialogueOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void manascore$renderCutscene(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!CutsceneClientHandler.isShowing()) return;
        CutsceneOverlay.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void manascore$renderDialogue(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        DialogueOverlay.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true), false);
    }
}
