/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.api.tab.AbstractInventoryTab;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.function.Predicate;

@Internal
public interface IInventoryTab {
    void sendOpenContainerPacket();

    @OnlyIn(Dist.CLIENT)
    default Predicate<Screen> isCurrentScreen() {
        return screen -> {
            AbstractInventoryTab tab = InventoryTabRegistry.findByScreen(screen);
            return tab != null && tab.equals(this);
        };
    }
}
