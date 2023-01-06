/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.tab;

import com.github.manasmods.manascore.tab.IInventoryTab;
import com.github.manasmods.manascore.tab.TabPosition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.AvailableSince;

@AvailableSince("1.0.0.0")
public abstract class AbstractInventoryTab extends Button implements IInventoryTab {
    protected static final int TAB_WIDTH = 28;
    protected static final int TAB_HEIGHT = 32;
    protected final Minecraft minecraft;
    @Getter
    @Setter
    private TabPosition position;

    public AbstractInventoryTab(Tooltip tooltip) {
        super(new Builder(Component.empty(), pButton -> {
                AbstractInventoryTab tab = (AbstractInventoryTab) pButton;
                tab.sendOpenContainerPacket();
            })
                .pos(0, 0)
                .size(TAB_WIDTH, TAB_HEIGHT)
                .tooltip(tooltip)
        );
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1f, 1f, 1f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        this.renderIcon(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    protected abstract void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.position.bindTexture();
        float yOffset = this.isCurrent() ? TAB_HEIGHT : 0F;
        blit(pPoseStack, this.getX(), this.getY(), TAB_WIDTH, TAB_HEIGHT, 0F, yOffset, TAB_WIDTH, TAB_HEIGHT - 1, TAB_WIDTH, TAB_HEIGHT * 2);
    }

    public boolean isCurrent() {
        return this.isCurrentScreen().test(minecraft.screen);
    }
}
