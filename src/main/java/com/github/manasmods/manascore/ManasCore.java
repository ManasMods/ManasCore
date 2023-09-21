/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore;

import com.github.manasmods.manascore.attribute.ManasCoreAttributes;
import com.github.manasmods.manascore.capability.skill.event.TickEventListenerHandler;
import com.github.manasmods.manascore.config.ManasCoreConfig;
import com.github.manasmods.manascore.network.ManasCoreNetwork;
import com.github.manasmods.manascore.skill.SkillRegistry;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        ModLoadingContext.get().registerConfig(Type.COMMON, ManasCoreConfig.SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ManasCoreAttributes.register(modEventBus);
        SkillRegistry.register(modEventBus);

        modEventBus.addListener(this::setup);

        final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(TickEventListenerHandler::onTick);
    }

    private void setup(final FMLCommonSetupEvent e) {
        ManasCoreNetwork.register();
    }
}
