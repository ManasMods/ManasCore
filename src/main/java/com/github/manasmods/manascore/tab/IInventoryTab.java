package com.github.manasmods.manascore.tab;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

public interface IInventoryTab {
    void sendOpenContainerPacket();

    @OnlyIn(Dist.CLIENT)
    default Predicate<Screen> isCurrentScreen() {
        return screen -> InventoryTabRegistry.findByScreen(screen) == this;
    }
}
