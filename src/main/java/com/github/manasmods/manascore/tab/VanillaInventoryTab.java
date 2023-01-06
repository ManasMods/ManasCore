/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.tab.AbstractInventoryTab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.Predicate;

@Internal
public class VanillaInventoryTab extends AbstractInventoryTab {
    private final ItemStack iconStack = new ItemStack(Blocks.GRASS_BLOCK);

    public VanillaInventoryTab() {
        super(Tooltip.create(Component.translatable("key.categories.inventory")));
    }

    @Override
    protected void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        minecraft.getItemRenderer().renderAndDecorateFakeItem(this.iconStack, this.getX() + 6, this.getY() + 8);
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
