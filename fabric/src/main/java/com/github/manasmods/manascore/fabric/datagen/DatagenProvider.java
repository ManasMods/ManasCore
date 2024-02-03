package com.github.manasmods.manascore.fabric.datagen;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.datagen.events.DatagenEvents;
import com.github.manasmods.manascore.fabric.datagen.helpers.FabricBlockLootHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.AdvancementHolder;

import java.util.function.Consumer;

public class DatagenProvider implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        ManasCore.Logger.info("Starting generation...");
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(AdvancementsGenerator::new);
        pack.addProvider(FabricBlockLootHelper::new);
    }

    static class AdvancementsGenerator extends FabricAdvancementProvider {

        protected AdvancementsGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateAdvancement(Consumer<AdvancementHolder> consumer) {
            DatagenEvents.registerAdvancementsEvent.invoker().registerAdvancements(consumer);
            ManasCore.Logger.info("Finished generation...");
        }
    }
}
