package com.github.manasmods.manascore.data.gen;

import com.github.manasmods.manascore.ManasCore;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class BlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {

    public BlockStateProvider(final GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    protected abstract void generate();

    @Override
    protected void registerStatesAndModels() {
        generate();
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block the target {@link Block}
     */
    protected void defaultBlock(Block block) {
        defaultBlock(block, block);
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block        the target {@link Block}
     * @param textureBlock the texture providing {@link Block}
     */
    protected void defaultBlock(Block block, Block textureBlock) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(cubeAll(textureBlock)).build());
        itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param stairBlock   the {@link StairBlock} Object
     * @param textureBlock the {@link Block} you want to use as texture
     */
    protected void stairs(Block stairBlock, Block textureBlock) {
        if (!(stairBlock instanceof StairBlock block)) {
            throw new IllegalArgumentException(Objects.requireNonNull(stairBlock.getRegistryName()) + " is not a instance of StairBlock.");
        } else {
            stairsBlock(block, new ResourceLocation(Objects.requireNonNull(textureBlock.getRegistryName()).getNamespace(), "block/" + textureBlock.getRegistryName().getPath()));
            itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
        }
    }

    protected void stairs(Block stairBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        if (stairBlock instanceof StairBlock block) {
            String baseName = block.getRegistryName().toString();
            ModelFile stairs = overlayStair(baseName, top, bottom, side, overlay);
            ModelFile stairsInner = overlayInnerStair(baseName + "_inner", top, bottom, side, overlay);
            ModelFile stairsOuter = overlayOuterStair(baseName + "_outer", top, bottom, side, overlay);

            getVariantBuilder(block)
                .forAllStatesExcept(state -> {
                    Direction facing = state.getValue(StairBlock.FACING);
                    Half half = state.getValue(StairBlock.HALF);
                    StairsShape shape = state.getValue(StairBlock.SHAPE);
                    int yRot = (int) facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason
                    if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                        yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
                    }
                    if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                        yRot += 90; // Top stairs are rotated 90 degrees clockwise
                    }
                    yRot %= 360;
                    boolean uvlock = yRot != 0 || half == Half.TOP; // Don't set uvlock for states that have no rotation
                    return ConfiguredModel.builder()
                        .modelFile(shape == StairsShape.STRAIGHT ? stairs : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner : stairsOuter)
                        .rotationX(half == Half.BOTTOM ? 0 : 180)
                        .rotationY(yRot)
                        .uvLock(uvlock)
                        .build();
                }, StairBlock.WATERLOGGED);

            this.itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + block.getRegistryName().getPath())));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(stairBlock.getRegistryName()) + " is not a instance of StairBlock.");
        }
    }

    protected void stairs(Block stairBlock, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        if (stairBlock instanceof StairBlock block) {
            stairsBlock(block, side, bottom, top);
            this.itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + block.getRegistryName().getPath())));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(stairBlock.getRegistryName()) + " is not a instance of StairBlock.");
        }
    }

    protected void stairs(Block stairBlock, ResourceLocation side, ResourceLocation top) {
        stairs(stairBlock, side, top, top);
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param slabBlock    the {@link SlabBlock} Object
     * @param textureBlock the {@link Block} you want to use as texture
     */
    protected void slab(Block slabBlock, Block textureBlock) {
        if (!(slabBlock instanceof SlabBlock block)) {
            throw new IllegalArgumentException(Objects.requireNonNull(slabBlock.getRegistryName()) + " is not a instance of StairBlock.");
        } else {
            ResourceLocation textureLocation = new ResourceLocation(Objects.requireNonNull(textureBlock.getRegistryName()).getNamespace(), "block/" + textureBlock.getRegistryName().getPath());
            slabBlock(block, textureLocation, textureLocation);
            itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
        }
    }

    protected void slab(Block slabBlock, Block fullBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        if (slabBlock instanceof SlabBlock block) {
            ModelFile doubleSlab = models().getExistingFile(new ResourceLocation(fullBlock.getRegistryName().getNamespace(), "block/" + fullBlock.getRegistryName().getPath()));
            ModelFile bottomSlab = overlaySlab(name(block), top, bottom, side, overlay);
            ModelFile topSlab = overlaySlabTop(name(block) + "_top", top, bottom, side, overlay);

            getVariantBuilder(block)
                .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottomSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(topSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleSlab));

            this.itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + block.getRegistryName().getPath())));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(slabBlock.getRegistryName()) + " is not a instance of StairBlock.");
        }
    }

    protected void slab(Block slabBlock, Block fullBlock, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        if (slabBlock instanceof SlabBlock block) {
            slabBlock(block, new ResourceLocation(fullBlock.getRegistryName().getNamespace(), "block/" + fullBlock.getRegistryName().getPath()), side, bottom, top);
            this.itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + block.getRegistryName().getPath())));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(slabBlock.getRegistryName()) + " is not a instance of SlabBlock.");
        }
    }

    protected void slab(Block stairBlock, Block fullBlock, ResourceLocation side, ResourceLocation top) {
        slab(stairBlock, fullBlock, side, top, top);
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block the {@link RotatedPillarBlock} Object.
     */
    protected void pillar(Block block) {
        if (!(block instanceof RotatedPillarBlock rotatedPillarBlock)) {
            throw new IllegalArgumentException(Objects.requireNonNull(block.getRegistryName()) + " is not a instance of RotatedPillarBlock.");
        } else {
            logBlock(rotatedPillarBlock);
            itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
        }
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block         the {@link RotatedPillarBlock} Object.
     * @param textureTopBot the path to the texture file for the top or bottom of the Block
     * @param textureSides  the path to the texture file for the sides of the block
     */
    protected void nonRotatablePillar(Block block, ResourceLocation textureTopBot, ResourceLocation textureSides) {
        getVariantBuilder(block)
            .forAllStates(blockState -> ConfiguredModel.builder().modelFile(models().cubeColumn(name(block), textureSides, textureTopBot)).build());
        itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
    }

    @SuppressWarnings("ConstantConditions")
    protected String name(Block block) {
        return block.getRegistryName().getPath();
    }

    private BlockModelBuilder overlayStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/overlay_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top)
            .texture("overlay", overlay);
    }

    private BlockModelBuilder overlayInnerStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/overlay_inner_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top)
            .texture("overlay", overlay);
    }

    private BlockModelBuilder overlayOuterStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/overlay_outer_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top)
            .texture("overlay", overlay);
    }

    private BlockModelBuilder overlaySlab(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/overlay_slab"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top)
            .texture("overlay", overlay);
    }

    private BlockModelBuilder overlaySlabTop(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/overlay_slab_top"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top)
            .texture("overlay", overlay);
    }
}

