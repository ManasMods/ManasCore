/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.tab;

import com.github.manasmods.manascore.ManasCore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ManasCore.MOD_ID, value = Dist.CLIENT)
public class ManasCoreInventoryTabs {
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent e) {
        InventoryTabRegistry.register(new VanillaInventoryTab());
    }
}
