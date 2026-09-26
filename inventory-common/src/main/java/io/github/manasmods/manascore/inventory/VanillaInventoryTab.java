/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.inventory;

import io.github.manasmods.manascore.inventory.api.AbstractInventoryTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Predicate;

import static io.github.manasmods.manascore.inventory.ManasCoreInventory.LOG;

public class VanillaInventoryTab extends AbstractInventoryTab {
    private final ItemStack iconStack = new ItemStack(Blocks.GRASS_BLOCK);

    public VanillaInventoryTab() {
        super(Tooltip.create(Component.translatable("key.categories.inventory")));
    }

    @Override
    protected void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var isTopRow = this.getCurrentTabIndex() < TABS_PER_ROW;
        guiGraphics.renderFakeItem(this.iconStack, this.getX() + 5, this.getY() + 8 + (isTopRow ? 1 : -1));
    }

    @Override
    public void sendOpenContainerPacket() {
        if (minecraft.player == null) {
            LOG.error("Local Player is null!?");
            return;
        }

        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    @Override
    public Predicate<Screen> isCurrentScreen() {
        return screen -> screen instanceof InventoryScreen;
    }
}
