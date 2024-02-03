package com.github.manasmods.manascore.fabric;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.datagen.events.DatagenEvents;
import com.github.manasmods.manascore.storage.fabric.StorageEventHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

public class ManasCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ManasCore.init();
        StorageEventHandler.init();

        DatagenEvents.registerAdvancementsEvent.register((consumer) -> {
            Advancement.Builder.advancement().display(new ItemStack(Items.ITEM_FRAME), Component.literal("Test"), Component.literal("test"), null, AdvancementType.GOAL, false, false, false)
                    .addCriterion("got_dirt", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIRT)).save(consumer, "test");
        });
        DatagenEvents.registerBlockLoot.register((helper) -> {
            helper.add(Blocks.BAMBOO_BLOCK, LootTable.lootTable());
        });
    }
}
