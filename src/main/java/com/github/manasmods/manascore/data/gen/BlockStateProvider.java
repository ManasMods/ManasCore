package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class BlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {

    public BlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper, String modId) {
        super(gen, modId, exFileHelper);
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
    protected void stairsBlock(Block stairBlock, Block textureBlock) {
        if (!(stairBlock instanceof StairBlock block)) {
            throw new IllegalArgumentException(Objects.requireNonNull(stairBlock.getRegistryName()) + " is not a instance of StairBlock.");
        } else {
            stairsBlock(block, new ResourceLocation(Objects.requireNonNull(textureBlock.getRegistryName()).getNamespace(), "block/" + textureBlock.getRegistryName().getPath()));
            itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
        }
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param slabBlock    the {@link SlabBlock} Object
     * @param textureBlock the {@link Block} you want to use as texture
     */
    protected void slabBlock(Block slabBlock, Block textureBlock) {
        if (!(slabBlock instanceof SlabBlock block)) {
            throw new IllegalArgumentException(Objects.requireNonNull(slabBlock.getRegistryName()) + " is not a instance of StairBlock.");
        } else {
            ResourceLocation textureLocation = new ResourceLocation(Objects.requireNonNull(textureBlock.getRegistryName()).getNamespace(), "block/" + textureBlock.getRegistryName().getPath());
            slabBlock(block, textureLocation, textureLocation);
            itemModels().getBuilder(Objects.requireNonNull(block.getRegistryName()).getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + block.getRegistryName().getPath())));
        }
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
}
