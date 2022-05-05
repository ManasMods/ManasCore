package com.github.manasmods.manascore.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ImagePredicateButton extends Button {
    private final ResourceLocation texture;
    private final RenderCheck renderCheck;

    public ImagePredicateButton(int pX, int pY, int pWidth, int pHeight, ResourceLocation texture, OnPress pOnPress, OnTooltip pOnToolTip, RenderCheck renderCheck) {
        super(pX, pY, pWidth, pHeight, new TextComponent(""), pOnPress, pOnToolTip);
        this.texture = texture;
        this.renderCheck = renderCheck;
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
        } else {
            blit(pPoseStack, x, y, 0, height, width, height, width, height * 2);
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        return super.clicked(pMouseX, pMouseY);
    }
}
