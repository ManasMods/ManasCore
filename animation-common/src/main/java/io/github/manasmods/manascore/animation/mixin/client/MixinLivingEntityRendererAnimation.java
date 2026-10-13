/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.animation.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.manasmods.manascore.animation.api.PlayerAnimationAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adjusts the {@link LivingEntityRenderer} for animations: it drops the render layers in first person and applies the root bone to entities that are not a player.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererAnimation<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private final Minecraft manascore$mc = Minecraft.getInstance();

    /**
     * Drops every render layer but the held item while a first-person animation plays, so the view shows bare arms like the vanilla hand overlay.
     * <p>
     * Hiding the body's {@link net.minecraft.client.model.geom.ModelPart}s in {@code PlayerRenderer} is not enough, because armour, elytra, cape and the other layers
     * build their own models with their own visibility. They would keep drawing around the missing body.
     * <p>
     * {@link ItemInHandLayer} is kept, because held items come from that layer once {@code ItemInHandRenderer} is cancelled for the animation.
     */
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private void manascore$dropLayersInFirstPerson(RenderLayer<T, M> layer, PoseStack poseStack, MultiBufferSource bufferSource, int light,
                                                   Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                                   float netHeadYaw, float headPitch, Operation<Void> original) {
        if (layer instanceof ItemInHandLayer || !manascore$hidesLayers(entity)) {
            original.call(layer, poseStack, bufferSource, light, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    /**
     * Applies the {@code body} bone of the animation as a whole entity transform for everything that is not a player,
     * at the point where vanilla finishes orienting the entity.
     * <p>
     * Players are skipped, because {@code MixinPlayerRendererAnimation} applies it at the return of {@code PlayerRenderer#setupRotations},
     * which calls this method as its super. Without the guard a player would get the root bone twice.
     * That mixin also handles the first-person branches, which never reach this method.
     *
     * @see PlayerAnimationAPI#applyRootBone
     */
    @Inject(method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
    private void manascore$applyRootBone(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick,
                                         float scale, CallbackInfo ci) {
        if (entity instanceof Player) return;
        PlayerAnimationAPI.applyRootBone(entity, poseStack);
    }

    /**
     * Determine if {@code entity} is the local player drawn into the world from their own eyes with a first-person animation playing.
     * This is the same condition the player renderer hides the body under.
     */
    @Unique
    private boolean manascore$hidesLayers(Entity entity) {
        return entity == manascore$mc.player && PlayerAnimationAPI.renderingLevel
                && manascore$mc.options.getCameraType().isFirstPerson()
                && entity instanceof Player player && PlayerAnimationAPI.state(player).firstPerson;
    }
}
