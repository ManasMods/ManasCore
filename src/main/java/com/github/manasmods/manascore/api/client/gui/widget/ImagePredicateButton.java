/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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

    public ImagePredicateButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation texture, OnPress pOnPress, Tooltip tooltip, RenderCheck renderCheck) {
        super(new Builder(Component.empty(), pOnPress)
            .pos(pX, pY)
            .size(pWidth, pHeight)
            .tooltip(tooltip)
        );
        this.texture = texture;
        this.renderCheck = renderCheck;
    }

    public void hideToolTip() {
        setTooltip(null);
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        active = renderCheck.check();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        if (active) {
            blit(pPoseStack, this.getX(), this.getY(), 0, isHoveredOrFocused() ? height : 0, width, height, width, height * 2);
        } else {
            blit(pPoseStack, this.getX(), this.getY(), 0, height, width, height, width, height * 2);
        }
    }
}
