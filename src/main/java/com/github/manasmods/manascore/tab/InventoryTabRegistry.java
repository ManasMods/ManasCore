/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.tab.annotation.ScreenForTab;
import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class InventoryTabRegistry {
    @Getter
    private static int nextEntryId = 1;
    private static final TreeMap<Integer, AbstractInventoryTab> registeredTabs = new TreeMap<>();

    public static void register(AbstractInventoryTab tab) {
        registeredTabs.put(nextEntryId++, tab);
    }

    public static Collection<AbstractInventoryTab> getValues() {
        return registeredTabs.values();
    }

    public static Map<Integer, AbstractInventoryTab> getEntries() {
        return Map.copyOf(registeredTabs);
    }

    @Nullable
    public static AbstractInventoryTab findByScreen(Screen screen) {
        if (!screen.getClass().isAnnotationPresent(ScreenForTab.class)) return null;
        ScreenForTab annotation = screen.getClass().getAnnotation(ScreenForTab.class);
        Optional<AbstractInventoryTab> result = getValues().stream()
            .filter(abstractInventoryTab -> annotation.value().isInstance(abstractInventoryTab))
            .findFirst();

        return result.orElse(null);
    }
}
