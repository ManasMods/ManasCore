/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.tab;

import com.github.manasmods.manascore.api.tab.annotation.ScreenForTab;
import com.github.manasmods.manascore.tab.InventoryTabRegistry;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@AvailableSince("1.0.0.0")
@NonExtendable
public final class InventoryTabs {
    /**
     * Registers a {@link AbstractInventoryTab} instance to the Inventory Tab Registry.
     */
    public static void registerTab(AbstractInventoryTab tab) {
        InventoryTabRegistry.register(tab);
    }

    public static Collection<AbstractInventoryTab> getRegisteredTabs() {
        return InventoryTabRegistry.getValues();
    }

    public static Map<Integer, AbstractInventoryTab> getRegistryEntries() {
        return InventoryTabRegistry.getEntries();
    }

    /**
     * Returns the registered {@link AbstractInventoryTab} Object or null.
     * The given {@link Screen} has be annotated with @{@link ScreenForTab} annotation to be able to find the {@link AbstractInventoryTab} in the registry.
     */
    @Nullable
    public static AbstractInventoryTab findByScreen(final Screen screen) {
        return InventoryTabRegistry.findByScreen(screen);
    }
}
