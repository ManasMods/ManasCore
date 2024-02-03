package com.github.manasmods.manascore.forge.datagen;

import com.github.manasmods.manascore.datagen.events.DatagenEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.function.Consumer;

public class AdvancmentProvider implements ForgeAdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider arg, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
        DatagenEvents.registerAdvancementsEvent.invoker().registerAdvancements(consumer);
    }
}
