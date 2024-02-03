package com.github.manasmods.manascore.forge.datagen.helpers;

import com.github.manasmods.manascore.datagen.events.DatagenEvents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;

import java.util.Set;

public class ForgeBlockLootHelper extends BlockLootSubProvider {
    protected ForgeBlockLootHelper(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures) {
        super(explosionResistant, enabledFeatures);
    }

    @Override
    protected void generate() {
        DatagenEvents.registerBlockLoot.invoker().registerBlockLoot(this);
    }
}
