package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.skill.SkillRegistry;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ManasCore.MOD_ID)
public class ManasCoreForge {
    public ManasCoreForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ManasCore.MOD_ID, modEventBus);
        ManasCore.init();
        // Skill API Workaround
        RegistrarManager.get(ManasCore.MOD_ID).get(SkillRegistry.KEY);
    }
}
