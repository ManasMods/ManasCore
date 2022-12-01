/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import java.util.Arrays;
import java.util.function.Supplier;

@AvailableSince("1.0.0.0")
public abstract class BlockTagProvider extends BlockTagsProvider {
    public BlockTagProvider(GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        generate();
    }

    @OverrideOnly
    protected abstract void generate();

    @SafeVarargs
    @NonExtendable
    protected final void mineableWithAxe(Supplier<? extends Block>... blocks) {
        mineableWithAxe(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void mineableWithAxe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void mineableWithHoe(Supplier<? extends Block>... blocks) {
        mineableWithHoe(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void mineableWithHoe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_HOE).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void mineableWithPickaxe(Supplier<? extends Block>... blocks) {
        mineableWithPickaxe(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void mineableWithPickaxe(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void mineableWithShovel(Supplier<? extends Block>... blocks) {
        mineableWithShovel(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void mineableWithShovel(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void mineableWithAllTools(Supplier<? extends Block>... blocks) {
        mineableWithAllTools(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void mineableWithAllTools(Block... blocks) {
        mineableWithAxe(blocks);
        mineableWithHoe(blocks);
        mineableWithPickaxe(blocks);
        mineableWithShovel(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void subTag(TagKey<Block> root, TagKey<Block> sub, Supplier<? extends Block>... blocks) {
        subTag(root, sub, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void subTag(TagKey<Block> root, TagKey<Block> sub, Block... blocks) {
        tag(root).addTag(sub);
        tag(sub).add(blocks);
    }
}
