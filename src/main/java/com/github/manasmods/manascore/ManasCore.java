/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import lombok.Getter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Mod(ManasCore.MOD_ID)
public final class ManasCore {
    public static final String MOD_ID = "manascore";
    @Getter
    private static final Logger logger = LogManager.getLogger();

    public ManasCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ManasCoreAttributes.register(modEventBus);
    }
}
