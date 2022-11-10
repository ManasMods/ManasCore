/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public abstract class ItemTagProvider extends ItemTagsProvider {

    public ItemTagProvider(GatherDataEvent gatherDataEvent, String modId, BlockTagProvider blockTagProvider) {
        super(gatherDataEvent.getGenerator(), blockTagProvider, modId, gatherDataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        generate();
    }

    protected abstract void generate();
}
