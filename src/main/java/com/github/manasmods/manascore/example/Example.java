package com.github.manasmods.manascore.example;

import com.github.manasmods.manascore.tab.AbstractInventoryTab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;


public class Example extends AbstractInventoryTab {
    private boolean isSelected = false;
    private final ItemStack iconStack = new ItemStack(Items.DIAMOND_HELMET);

    public Example() {
        super(new TranslatableComponent("example"));
    }

    @Override
    protected void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        minecraft.getItemRenderer().renderAndDecorateFakeItem(this.iconStack, this.x + 6, this.y + 8);
    }

    @Override
    public void sendOpenContainerPacket() {
        this.isSelected = !this.isSelected;
    }

    @Override
    public Predicate<Screen> isCurrentScreen() {
        return screen -> this.isSelected;
    }
}
