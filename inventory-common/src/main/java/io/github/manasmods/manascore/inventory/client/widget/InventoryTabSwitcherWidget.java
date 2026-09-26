/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.inventory.client.widget;

import io.github.manasmods.manascore.inventory.api.AbstractInventoryTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.TreeMap;

public class InventoryTabSwitcherWidget extends AbstractWidget {
    private int page = 1;
    private final int maxPages;
    private final Button prevButton, nextButton;
    private final TreeMap<Integer, AbstractInventoryTab> tabs = new TreeMap<>();
    private final AbstractContainerScreen parent;
    private boolean isFocused = false;

    public InventoryTabSwitcherWidget(AbstractContainerScreen parent, int maxPages) {
        super(0, 0, parent.width, 0, Component.empty());
        this.parent = parent;
        this.maxPages = maxPages;
        this.prevButton = Button.builder(Component.literal("<"), pButton -> {
                    page = Math.max(page - 1, 1);
                    updateTabs();
                })
                .pos(this.parent.leftPos - 20 - 2, this.parent.topPos - 20 - 2)
                .tooltip(Tooltip.create(Component.translatable("createWorld.customize.custom.prev")))
                .size(20, 20)
                .build();
        this.nextButton = Button.builder(Component.literal(">"), pButton -> {
                    page = Math.min(page + 1, this.maxPages);
                    updateTabs();
                })
                .pos(parent.leftPos + parent.imageWidth + 2, this.parent.topPos - 20 - 2)
                .tooltip(Tooltip.create(Component.translatable("createWorld.customize.custom.next")))
                .size(20, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (page > 1) {
            this.prevButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        if (maxPages > 1 && page != maxPages) {
            this.nextButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        if (maxPages > 1) {
            var font = Minecraft.getInstance().font;
            guiGraphics.drawCenteredString(font, this.page + " / " + this.maxPages, this.parent.width / 2, this.parent.topPos - font.lineHeight - CreativeModeInventoryScreen.TAB_HEIGHT, Color.WHITE.getRGB());
        }

        for (var tab : this.tabs.values()) {
            if (!tab.isActive()) continue;
            tab.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (var tab : this.tabs.values()) {
            if (!tab.isActive()) continue;
            tab.renderBg(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void updateTabs() {
        this.tabs.forEach((tabIndex, widget) -> {
            int tabScreenIndex = (tabIndex % (AbstractInventoryTab.TABS_PER_ROW * 2));

            double totalTabsOnPage = AbstractInventoryTab.TABS_PER_ROW * 2;
            boolean isVisible = Math.floor((tabIndex - 1) / totalTabsOnPage) == (this.page - 1);
            widget.active = isVisible;

            if (!isVisible) return;

            final int yOffset = 4;
            final int xOffset = 1;

            switch (tabScreenIndex) {
                case 1 -> {
                    widget.setX(parent.leftPos);
                    widget.setY(parent.topPos - widget.getHeight() + yOffset);
                    widget.setCurrentTabIndex(tabScreenIndex);
                }
                case 2, 3, 4, 5 -> {
                    widget.setX(parent.leftPos + widget.getWidth() * (tabScreenIndex - 1) + (tabScreenIndex * 3));
                    widget.setY(parent.topPos - widget.getHeight() + yOffset);
                    widget.setCurrentTabIndex(tabScreenIndex);
                }
                case 6 -> {
                    widget.setX(parent.leftPos + widget.getWidth() * (tabScreenIndex - 1) + (xOffset * tabScreenIndex - 1) + 15);
                    widget.setY(parent.topPos - widget.getHeight() + yOffset);
                    widget.setCurrentTabIndex(tabScreenIndex);
                }
                case 7 -> {
                    widget.setX(parent.leftPos);
                    widget.setY(parent.topPos + parent.imageWidth - yOffset - 10);
                    widget.setCurrentTabIndex(tabScreenIndex);
                }
                case 8, 9, 10, 11 -> {
                    widget.setX(parent.leftPos + widget.getWidth() * (tabScreenIndex - AbstractInventoryTab.TABS_PER_ROW - 1) + ((tabScreenIndex - AbstractInventoryTab.TABS_PER_ROW) * 3));
                    widget.setY(parent.topPos + parent.imageWidth - yOffset - 10);
                    widget.setCurrentTabIndex(tabScreenIndex);
                }
                case 0 -> {
                    widget.setX(parent.leftPos + widget.getWidth() * (AbstractInventoryTab.TABS_PER_ROW - 1) + (xOffset * AbstractInventoryTab.TABS_PER_ROW - 1) + 15);
                    widget.setY(parent.topPos + parent.imageWidth - yOffset - 10);
                    widget.setCurrentTabIndex(AbstractInventoryTab.TABS_PER_ROW * 2);
                }
            }
        });
    }

    public void addUpdateListener(int index, AbstractInventoryTab widget) {
        tabs.put(index, widget);
        updateTabs();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.tabs.values()
                    .stream()
                    .filter(AbstractInventoryTab::isActive)
                    .forEach(abstractInventoryTab -> abstractInventoryTab.mouseClicked(pMouseX, pMouseY, pButton));
            if (this.prevButton.isMouseOver(pMouseX, pMouseY)) this.prevButton.mouseClicked(pMouseX, pMouseY, pButton);
            if (this.nextButton.isMouseOver(pMouseX, pMouseY)) this.nextButton.mouseClicked(pMouseX, pMouseY, pButton);
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    @Override
    public boolean isFocused() {
        return isFocused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        if (isFocused()) return NarrationPriority.FOCUSED;
        return NarrationPriority.NONE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.nextButton.updateWidgetNarration(narrationElementOutput);
        this.prevButton.updateWidgetNarration(narrationElementOutput);
    }
}
