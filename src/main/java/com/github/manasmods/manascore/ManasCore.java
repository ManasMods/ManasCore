package com.github.manasmods.manascore;

import com.github.manasmods.manascore.proxy.ManasClientProxy;
import com.github.manasmods.manascore.proxy.ManasCoreCommon;
import com.github.manasmods.manascore.proxy.ManasServerProxy;
import lombok.Getter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ManasCore.MOD_ID)
public class ManasCore {
    public static final String MOD_ID = "manascore";
    private static final Logger LOGGER = LogManager.getLogger();
    @Getter
    private final ManasCoreCommon proxy;

    public ManasCore() {
        proxy = DistExecutor.safeRunForDist(() -> ManasClientProxy::new, () -> ManasServerProxy::new);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        proxy.preInit(modEventBus);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
