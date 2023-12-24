package com.github.manasmods.manascore.neoforge;

import com.github.manasmods.manascore.ManasCore;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ManasCore.MOD_ID)
public class ManasCoreNeoForge {
    public ManasCoreNeoForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ManasCore.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ManasCore.init();
    }
}
