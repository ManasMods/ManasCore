/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.client.gui.widget;

import com.github.manasmods.manascore.api.tab.AbstractInventoryTab;
import com.github.manasmods.manascore.core.AbstractContainerScreenAccessor;
import com.github.manasmods.manascore.tab.TabPosition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.TreeMap;

@Internal
public class InventoryTabSwitcherWidget extends GuiComponent implements Renderable, GuiEventListener {
    private int page = 1;
    private final int maxPages;
    private final Button prevButton, nextButton;
    private final TreeMap<Integer, AbstractInventoryTab> tabs = new TreeMap<>();
    private final AbstractContainerScreen parent;

    public InventoryTabSwitcherWidget(AbstractContainerScreen parent, int maxPages) {
        this.parent = parent;
        this.maxPages = maxPages;
        this.prevButton = Button.builder(Component.literal("<"), pButton -> {
                page = Math.max(page - 1, 1);
                updateTabs();
            })
            .pos(this.parent.getGuiLeft() - 20 - 2, this.parent.getGuiTop() - 20 - 2)
            .size(20, 20)
            .build();
        this.nextButton = Button.builder(Component.literal(">"), pButton -> {
                page = Math.min(page + 1, this.maxPages);
                updateTabs();
            })
            .pos(parent.getGuiLeft() + ((AbstractContainerScreenAccessor) parent).getImageWidth() + 2, this.parent.getGuiTop() - 20 - 2)
            .size(20, 20)
            .build();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (page > 1) {
            this.prevButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        if (maxPages > 1 && page != maxPages) {
            this.nextButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        if (maxPages > 1) {
            drawCenteredString(poseStack, Minecraft.getInstance().font, this.page + " / " + this.maxPages, this.parent.width / 2, 2, Color.WHITE.getRGB());
        }

        this.tabs.values()
            .stream()
            .filter(AbstractInventoryTab::isActive)
            .forEach(abstractInventoryTab -> abstractInventoryTab.render(poseStack, mouseX, mouseY, partialTicks));
    }

    public void updateTabs() {
        this.tabs.forEach((integer, widget) -> {
            int tabScreenIndex = integer;
            while (tabScreenIndex > 12) {
                tabScreenIndex -= 12;
            }

            final int yOffset = 4;
            final int xOffset = 1;

            switch (tabScreenIndex) {
                case 1 -> {
                    widget.setX(parent.getGuiLeft());
                    widget.setY(parent.getGuiTop() - widget.getHeight() + yOffset);
                    widget.setPosition(TabPosition.LEFT_TOP);
                }
                case 2, 3 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 1) + (xOffset * tabScreenIndex - 1) + 1);
                    widget.setY(parent.getGuiTop() - widget.getHeight() + yOffset);
                    widget.setPosition(TabPosition.TOP);
                }
                case 4, 5 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 1) + (xOffset * tabScreenIndex - 1) + 2);
                    widget.setY(parent.getGuiTop() - widget.getHeight() + yOffset);
                    widget.setPosition(TabPosition.TOP);
                }
                case 6 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 1) + (xOffset * tabScreenIndex - 1) + 3);
                    widget.setY(parent.getGuiTop() - widget.getHeight() + yOffset);
                    widget.setPosition(TabPosition.RIGHT_TOP);
                }
                case 7 -> {
                    widget.setX(parent.getGuiLeft());
                    widget.setY(parent.getGuiTop() + ((AbstractContainerScreenAccessor) parent).getImageWidth() - yOffset - 11);
                    widget.setPosition(TabPosition.LEFT_BOT);
                }
                case 8, 9 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 7) + (xOffset * tabScreenIndex - 7) + 1);
                    widget.setY(parent.getGuiTop() + ((AbstractContainerScreenAccessor) parent).getImageWidth() - yOffset - 11);
                    widget.setPosition(TabPosition.BOT);
                }
                case 10, 11 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 7) + (xOffset * tabScreenIndex - 7) + 2);
                    widget.setY(parent.getGuiTop() + ((AbstractContainerScreenAccessor) parent).getImageWidth() - yOffset - 11);
                    widget.setPosition(TabPosition.BOT);
                }
                case 12 -> {
                    widget.setX(parent.getGuiLeft() + widget.getWidth() * (tabScreenIndex - 7) + (xOffset * tabScreenIndex - 7) + 3);
                    widget.setY(parent.getGuiTop() + ((AbstractContainerScreenAccessor) parent).getImageWidth() - yOffset - 11);
                    widget.setPosition(TabPosition.RIGHT_BOT);
                }
            }

            boolean isVisible = Math.ceil(integer / 12F) == this.page;
            widget.active = isVisible;
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
}
