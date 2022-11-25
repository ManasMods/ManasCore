/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@AvailableSince("1.0.0.0")
public abstract class ItemTagProvider extends ItemTagsProvider {

    public ItemTagProvider(GatherDataEvent gatherDataEvent, String modId, BlockTagProvider blockTagProvider) {
        super(gatherDataEvent.getGenerator(), blockTagProvider, modId, gatherDataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        generate();
    }

    @OverrideOnly
    protected abstract void generate();
}
