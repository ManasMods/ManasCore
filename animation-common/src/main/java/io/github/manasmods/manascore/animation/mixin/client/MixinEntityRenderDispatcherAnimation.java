/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcherAnimation {

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void renderShadow(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity,
                                               float opacity, float tickDelta, LevelReader world, float radius, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (entity instanceof Player player && mc.options.getCameraType().isFirstPerson() && player == mc.player
                && PlayerAnimationAPI.renderingLevel && PlayerAnimationAPI.state(player).firstPerson) ci.cancel();
    }
}
