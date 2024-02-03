package com.github.manasmods.manascore.fabric.datagen.helpers;

import com.github.manasmods.manascore.datagen.events.DatagenEvents;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Function;

public class FabricBlockLootHelper extends FabricBlockLootTableProvider {
    public FabricBlockLootHelper(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        DatagenEvents.registerBlockLoot.invoker().registerBlockLoot(this);
    }
}
