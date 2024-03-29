/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.tab.AbstractInventoryTab;
import com.github.manasmods.manascore.client.gui.widget.InventoryTabSwitcherWidget;
import com.github.manasmods.manascore.api.tab.annotation.ScreenForTab;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Map;

@Internal
@Mod.EventBusSubscriber(modid = ManasCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InventoryTabHandler {

    @SubscribeEvent
    public static void onOpenTabMenu(final ScreenEvent.Init e) {
        if (!isValidTabScreen(e.getScreen())) return;
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        final Map<Integer, AbstractInventoryTab> tabRegistryEntries = InventoryTabRegistry.getEntries();
        InventoryTabSwitcherWidget tabSwitcherWidget = new InventoryTabSwitcherWidget(containerScreen, (int) Math.round(Math.ceil(tabRegistryEntries.size() / 12F)));

        tabRegistryEntries.forEach(tabSwitcherWidget::addUpdateListener);
        tabSwitcherWidget.updateTabs();
        e.addListener(tabSwitcherWidget);
    }

    private static boolean isValidTabScreen(Screen screen) {
        if (screen instanceof InventoryScreen) return true;
        return screen.getClass().isAnnotationPresent(ScreenForTab.class);
    }
}
