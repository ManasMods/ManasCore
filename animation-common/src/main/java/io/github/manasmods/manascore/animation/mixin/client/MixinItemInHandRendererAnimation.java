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

/**
 * Cancels the vanilla first-person hands while a first-person animation is active, because the animation is drawn through the player model instead.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRendererAnimation {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();
    @Unique
    private EntityRenderDispatcher manascore$dispatcher = null;

    /**
     * Cancels the vanilla hands while a first-person animation is active.
     * With an animation that is not first person, or is being reset, the player model is posed once through {@code setupAnim}.
     */
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void renderHandsWithItems(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                      LocalPlayer localPlayer, int light, CallbackInfo ci) {
        if (localPlayer == null || manascore$mc.player != localPlayer || manascore$mc.screen != null) return;
        if (manascore$dispatcher == null) manascore$dispatcher = manascore$mc.getEntityRenderDispatcher();

        // Pose player model
        PlayerAnimationAPI.PlayerAnimationState state = PlayerAnimationAPI.state(localPlayer);
        if (!state.currentAnimation.isEmpty() && (!state.firstPerson || state.reset)) {
            PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) manascore$dispatcher.getRenderer(localPlayer)).getModel();
            model.setupAnim(localPlayer, 0.0F, 0.0F, (float) localPlayer.tickCount + partialTicks, 0.0F, 0.0F);
        }

        // Cancel vanilla hands
        if (state.firstPerson) ci.cancel();
    }
}
