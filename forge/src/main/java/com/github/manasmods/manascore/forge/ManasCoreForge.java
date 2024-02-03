package com.github.manasmods.manascore.forge;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.forge.datagen.AdvancmentProvider;
import com.github.manasmods.manascore.skill.SkillRegistry;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

@Mod(ManasCore.MOD_ID)
public class ManasCoreForge {
    public ManasCoreForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ManasCore.MOD_ID, modEventBus);
        ManasCore.init();
        // Skill API Workaround
        RegistrarManager.get(ManasCore.MOD_ID).get(SkillRegistry.KEY);

        modEventBus.register(this);
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(),
                (DataProvider.Factory<ForgeAdvancementProvider>) output -> new ForgeAdvancementProvider(
                        output,
                        event.getLookupProvider(),
                        event.getExistingFileHelper(),
                        // Sub providers which generate the advancements
                        List.of(new AdvancmentProvider())
                )
        );
    }
}
