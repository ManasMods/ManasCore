package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public abstract class BlockTagProvider extends BlockTagsProvider {
    public BlockTagProvider(GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        generate();
    }

    protected abstract void generate();

    protected void mineableWithAxe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(blocks);
    }

    protected void mineableWithHoe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_HOE).add(blocks);
    }

    protected void mineableWithPickaxe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(blocks);
    }

    protected void mineableWithShovel(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(blocks);
    }

    protected void mineableWithAllTools(Block... blocks) {
        mineableWithAxe(blocks);
        mineableWithHoe(blocks);
        mineableWithPickaxe(blocks);
        mineableWithShovel(blocks);
    }
}
