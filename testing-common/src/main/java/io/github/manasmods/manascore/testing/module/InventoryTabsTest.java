/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.testing.module;

import io.github.manasmods.manascore.inventory.api.AbstractInventoryTab;
import io.github.manasmods.manascore.inventory.api.InventoryTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.github.manasmods.manascore.testing.ManasCoreTesting.LOG;

public class InventoryTabsTest {

    public static void init(int tabCount) {
        List<Item> itemFields = Arrays.stream(Items.class.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
                .filter(field -> field.getType().equals(Item.class))
                .map(field -> {
                    try {
                        return (Item) field.get(null);
                    } catch (IllegalAccessException e) {
                        LOG.error("Failed to get Item from field: {}", field.getName());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(item -> item != Items.AIR)
                .toList();

        for (int i = 0; i < Math.min(itemFields.size(), tabCount); i++) {
            Item item = itemFields.get(i);
            InventoryTabs.registerTab(new TestInventoryTab(i, item));
            LOG.info("Registered Test Tab {}", i);
        }
    }

    private static class TestInventoryTab extends AbstractInventoryTab {
        private static int selectedIndex = -1;
        private final ItemStack iconStack;
        private final int index;

        public TestInventoryTab(int index, Item icon) {
            super(Tooltip.create(Component.literal("Test Tab - " + index)));
            this.iconStack = icon.getDefaultInstance();
            this.index = index;
        }

        @Override
        protected void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var isTopRow = this.getCurrentTabIndex() < TABS_PER_ROW;
            guiGraphics.renderFakeItem(this.iconStack, this.getX() + 5, this.getY() + 8 + (isTopRow ? 1 : -1));
        }

        @Override
        public void sendOpenContainerPacket() {
            LOG.info("Called Test Tab {} opening request", this.index);
            selectedIndex = selectedIndex == this.index ? -1 : this.index;
        }

        @Override
        public boolean isCurrent() {
            return selectedIndex == this.index;
        }
    }
}
