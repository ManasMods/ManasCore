package de.blutmondgilde.manascore.forge;

import dev.architectury.platform.forge.EventBuses;
import de.blutmondgilde.manascore.ManasCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ManasCore.MOD_ID)
public class ManasCoreForge {
    public ManasCoreForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ManasCore.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ManasCore.init();
    }
}