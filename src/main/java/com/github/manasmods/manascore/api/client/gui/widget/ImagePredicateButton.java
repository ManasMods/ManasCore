/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

/**
 * Simple button which gets rendered while the render check is true
 */
@AvailableSince("1.0.0.0")
public class ImagePredicateButton extends Button {
    private final ResourceLocation texture;
    private final RenderCheck renderCheck;
    private boolean showToolTip = true;

    public ImagePredicateButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation texture, OnPress pOnPress, OnTooltip pOnToolTip, RenderCheck renderCheck) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, pOnToolTip);
        this.texture = texture;
        this.renderCheck = renderCheck;
    }

    public void hideToolTip() {
        showToolTip = false;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        active = renderCheck.check();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (active) {
            blit(pPoseStack, x, y, 0, isHoveredOrFocused() ? height : 0, width, height, width, height * 2);
            if (this.isHoveredOrFocused() && showToolTip) {
                this.renderToolTip(pPoseStack, pMouseX, pMouseY);
            }
        } else {
            blit(pPoseStack, x, y, 0, height, width, height, width, height * 2);
        }
    }
}
