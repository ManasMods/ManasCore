/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.inventory.api;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractInventoryTab extends Button implements InventoryTab {
    public static final int TABS_PER_ROW = 6;
    protected final Minecraft minecraft;
    @Getter
    @Setter
    private int currentTabIndex;

    public AbstractInventoryTab(Tooltip tooltip) {
        super(0, 0, CreativeModeInventoryScreen.TAB_WIDTH, CreativeModeInventoryScreen.TAB_HEIGHT, Component.empty(), button -> {
            AbstractInventoryTab tab = (AbstractInventoryTab) button;
            tab.sendOpenContainerPacket();
        }, Button.DEFAULT_NARRATION);
        setTooltip(tooltip);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        renderIcon(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().popPose();

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected abstract void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

    public void renderBg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation[] textureSource = this.currentTabIndex <= TABS_PER_ROW
                ? isCurrent() ? CreativeModeInventoryScreen.SELECTED_TOP_TABS : CreativeModeInventoryScreen.UNSELECTED_TOP_TABS
                : isCurrent() ? CreativeModeInventoryScreen.SELECTED_BOTTOM_TABS : CreativeModeInventoryScreen.UNSELECTED_BOTTOM_TABS;
        int textureIndex = (this.currentTabIndex - 1) % TABS_PER_ROW;
        ResourceLocation texture = textureSource[this.currentTabIndex % TABS_PER_ROW == 0 ? textureSource.length - 1 : textureIndex];

        if (isCurrent()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0F, 0F, 1F);
        }
        guiGraphics.blitSprite(texture, this.getX(), this.getY(), CreativeModeInventoryScreen.TAB_WIDTH, CreativeModeInventoryScreen.TAB_HEIGHT);
        if (isCurrent()) {
            guiGraphics.pose().popPose();
        }
    }

    public boolean isCurrent() {
        return this.isCurrentScreen().test(minecraft.screen);
    }
}
