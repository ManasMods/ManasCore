/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractInventoryTab extends Button implements IInventoryTab {
    protected static final int TAB_WIDTH = 28;
    protected static final int TAB_HEIGHT = 32;
    private final List<FormattedCharSequence> tooltip;
    protected final Minecraft minecraft;
    @Getter
    @Setter
    private TabPosition position;

    public AbstractInventoryTab(MutableComponent tooltip, MutableComponent... tooltips) {
        super(0, 0, TAB_WIDTH, TAB_HEIGHT, new TextComponent(""), pButton -> {
            AbstractInventoryTab tab = (AbstractInventoryTab) pButton;
            tab.sendOpenContainerPacket();
        }, (pButton, pPoseStack, pMouseX, pMouseY) -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen == null) return;

            AbstractInventoryTab tab = (AbstractInventoryTab) pButton;
            screen.renderTooltip(pPoseStack, tab.tooltip, pMouseX, pMouseY);
        });

        this.minecraft = Minecraft.getInstance();
        this.tooltip = Stream.concat(Stream.of(tooltip), Arrays.stream(tooltips))
            .map(Component::getVisualOrderText)
            .toList();
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1f, 1f, 1f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        this.renderIcon(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.isHoveredOrFocused()) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }

    protected abstract void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.position.bindTexture();
        float yOffset = this.isCurrent() ? TAB_HEIGHT : 0F;
        blit(pPoseStack, this.x, this.y, TAB_WIDTH, TAB_HEIGHT, 0F, yOffset, TAB_WIDTH, TAB_HEIGHT - 1, TAB_WIDTH, TAB_HEIGHT * 2);
    }

    public boolean isCurrent() {
        return this.isCurrentScreen().test(minecraft.screen);
    }
}
