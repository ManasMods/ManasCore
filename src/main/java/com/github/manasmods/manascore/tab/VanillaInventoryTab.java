/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Predicate;


public class VanillaInventoryTab extends AbstractInventoryTab {
    private final ItemStack iconStack = new ItemStack(Blocks.GRASS_BLOCK);

    public VanillaInventoryTab() {
        super(new TranslatableComponent("key.categories.inventory"));
    }

    @Override
    protected void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        minecraft.getItemRenderer().renderAndDecorateFakeItem(this.iconStack, this.x + 6, this.y + 8);
    }

    @Override
    public void sendOpenContainerPacket() {
        if (minecraft.player == null) {
            ManasCore.getLogger().fatal("Local Player is null!?");
            return;
        }

        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    @Override
    public Predicate<Screen> isCurrentScreen() {
        return screen -> screen instanceof InventoryScreen;
    }
}
