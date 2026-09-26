/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRendererAnimation {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();
    @Unique
    private EntityRenderDispatcher manascore$dispatcher = null;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void renderHandsWithItems(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                      LocalPlayer localPlayer, int light, CallbackInfo ci) {
        if (localPlayer == null || manascore$mc.player != localPlayer || manascore$mc.screen != null) return;
        if (manascore$dispatcher == null) manascore$dispatcher = manascore$mc.getEntityRenderDispatcher();

        PlayerAnimationAPI.PlayerAnimationState state = PlayerAnimationAPI.state(localPlayer);
        if (!state.currentAnimation.isEmpty() && (!state.firstPerson || state.reset)) {
            PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) manascore$dispatcher.getRenderer(localPlayer)).getModel();
            model.setupAnim(localPlayer, 0.0F, 0.0F, (float) localPlayer.tickCount + partialTicks, 0.0F, 0.0F);
        }

        if (state.firstPerson) ci.cancel();
    }
}
