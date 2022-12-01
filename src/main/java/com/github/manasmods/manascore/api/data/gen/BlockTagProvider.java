/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;
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
    protected final void needsNetheriteTool(Supplier<? extends Block>... blocks) {
        needsNetheriteTool(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsNetheriteTool(Block... blocks) {
        tag(Blocks.NEEDS_NETHERITE_TOOL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void needsDiamondTool(Supplier<? extends Block>... blocks) {
        needsDiamondTool(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsDiamondTool(Block... blocks) {
        tag(BlockTags.NEEDS_DIAMOND_TOOL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void needsIronTool(Supplier<? extends Block>... blocks) {
        needsIronTool(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsIronTool(Block... blocks) {
        tag(BlockTags.NEEDS_IRON_TOOL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void needsStoneTool(Supplier<? extends Block>... blocks) {
        needsStoneTool(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsStoneTool(Block... blocks) {
        tag(BlockTags.NEEDS_STONE_TOOL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void needsWoodenTools(Supplier<? extends Block>... blocks) {
        needsWoodenTools(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsWoodenTools(Block... blocks) {
        tag(Blocks.NEEDS_WOOD_TOOL).add(blocks);
    }

    @SafeVarargs
    @NonExtendable
    protected final void needsGoldenTools(Supplier<? extends Block>... blocks) {
        needsGoldenTools(Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new));
    }

    protected void needsGoldenTools(Block... blocks) {
        tag(Blocks.NEEDS_GOLD_TOOL).add(blocks);
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
