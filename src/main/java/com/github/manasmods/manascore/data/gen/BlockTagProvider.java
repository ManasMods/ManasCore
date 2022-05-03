package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public abstract class BlockTagProvider extends BlockTagsProvider {
    public BlockTagProvider(GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        generate();
    }

    protected void generate(){

    }
}
