/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@AvailableSince("1.0.0.0")
public abstract class ItemTagProvider extends ItemTagsProvider {

    public ItemTagProvider(GatherDataEvent gatherDataEvent, String modId, CompletableFuture<TagLookup<Block>> blockTagProvider) {
        super(gatherDataEvent.getGenerator().getPackOutput(),gatherDataEvent.getLookupProvider(), blockTagProvider, modId, gatherDataEvent.getExistingFileHelper());
    }

    @SafeVarargs
    @NonExtendable
    protected final void subTag(TagKey<Item> root, TagKey<Item> sub, Supplier<? extends Item>... items) {
        subTag(root, sub, Arrays.stream(items).map(Supplier::get).toArray(Item[]::new));
    }

    private void subTag(TagKey<Item> root, TagKey<Item> sub, Item... items) {
        tag(root).addTag(sub);
        tag(sub).add(items);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        generate();
    }

    @OverrideOnly
    protected abstract void generate();
}
