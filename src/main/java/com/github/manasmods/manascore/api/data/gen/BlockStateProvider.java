/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import com.github.manasmods.manascore.ManasCore;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockModels;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockModels.CubeAllModel;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockModels.PillarModel;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockModels.SlabModel;
import com.github.manasmods.manascore.api.data.gen.annotation.GenerateBlockModels.StairModel;
import com.github.manasmods.manascore.api.util.ReflectionUtils;
import lombok.extern.log4j.Log4j2;
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
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@AvailableSince("1.0.0.0")
@SuppressWarnings("unused")
@Log4j2
public abstract class BlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {
    private static final Type GEN_ANNOTATION = Type.getType(GenerateBlockModels.class);

    public BlockStateProvider(final GatherDataEvent gatherDataEvent, String modId) {
        super(gatherDataEvent.getGenerator(), modId, gatherDataEvent.getExistingFileHelper());
    }

    @OverrideOnly
    protected abstract void generate();

    @NonExtendable
    @Override
    protected final void registerStatesAndModels() {
        generate();
        final List<AnnotationData> annotations = new ArrayList<>();
        ModList.get().forEachModFile(modFile -> {
            modFile.getScanResult().getAnnotations()
                .stream()
                .filter(annotationData -> GEN_ANNOTATION.equals(annotationData.annotationType()))
                .forEach(annotations::add);
        });
        generateAnnotationModels(annotations);
    }

    @NonExtendable
    private void generateAnnotationModels(List<AnnotationData> annotations) {
        annotations.forEach(annotationData -> {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(annotationData.clazz().getClassName());
            } catch (ClassNotFoundException e) {
                log.error("Could not load class " + annotationData.clazz().getClassName());
                log.throwing(e);
            }
            if (clazz == null) return;

            List<Field> blockRegistryObjectFieldList = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType().equals(RegistryObject.class))
                .filter(field -> {
                    ParameterizedType registryObjectType = null;
                    try {
                        registryObjectType = ((ParameterizedType) field.getGenericType());
                    } catch (ClassCastException e) {
                        log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName());
                        log.throwing(e);
                    }
                    if (registryObjectType == null) return false;

                    Class<?> registryObjectClass = null;
                    try {
                        registryObjectClass = (Class<?>) registryObjectType.getActualTypeArguments()[0];
                    } catch (ClassCastException e) {
                        log.error("Could not load generic type of field " + field.getName() + " in " + annotationData.clazz().getClassName() + ".");
                        log.throwing(e);
                    }
                    if (registryObjectClass == null) return false;

                    return Block.class.isAssignableFrom(registryObjectClass);
                })
                .toList();

            generateCubeAllModels(annotationData, blockRegistryObjectFieldList);
            generatePillarModels(annotationData, blockRegistryObjectFieldList);
            generateStairModels(annotationData, blockRegistryObjectFieldList);
            generateSlabModels(annotationData, blockRegistryObjectFieldList);
        });
    }

    @NonExtendable
    private void generateCubeAllModels(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(CubeAllModel.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);

            if (registryObject == null) continue;
            CubeAllModel annotation = registryObjectField.getAnnotation(CubeAllModel.class);
            // Generate default model
            if (annotation.value().isEmpty() || annotation.value().isBlank()) {
                log.debug("Generating block model for registry object {}", registryObject.getId());
                defaultBlock(registryObject.get(), annotation.renderType());
                continue;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Block textureBlock = ForgeRegistries.BLOCKS.getValue(itemId);
            if (textureBlock == null) {
                log.error("Could not find texture block {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block model for registry object {} with texture of {}", registryObject.getId(), itemId);
            defaultBlock(registryObject.get(), textureBlock, annotation.renderType());
        }
    }

    @NonExtendable
    private void generatePillarModels(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(PillarModel.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);
            if (registryObject == null) continue;
            // Generate default model
            log.debug("Generating block model for registry object {}", registryObject.getId());
            pillar(registryObject.get());
        }
    }

    @NonExtendable
    private void generateStairModels(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(StairModel.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);

            if (registryObject == null) continue;
            StairModel annotation = registryObjectField.getAnnotation(StairModel.class);
            // Generate default model
            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Block textureBlock = ForgeRegistries.BLOCKS.getValue(itemId);
            if (textureBlock == null) {
                log.error("Could not find texture block {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block model for registry object {} with texture of {}", registryObject.getId(), itemId);
            stairs(registryObject.get(), textureBlock);
        }
    }

    @NonExtendable
    private void generateSlabModels(AnnotationData annotationData, List<Field> blockRegistryObjectFieldList) {
        for (Field registryObjectField : blockRegistryObjectFieldList) {
            if (!registryObjectField.isAnnotationPresent(SlabModel.class)) continue;
            RegistryObject<Block> registryObject = ReflectionUtils.getRegistryObjectFromField(annotationData, registryObjectField, Block.class);

            if (registryObject == null) continue;
            SlabModel annotation = registryObjectField.getAnnotation(SlabModel.class);
            // Generate default model
            ResourceLocation itemId = ResourceLocation.tryParse(annotation.value());
            Block textureBlock = ForgeRegistries.BLOCKS.getValue(itemId);
            if (textureBlock == null) {
                log.error("Could not find texture block {} for block {}", itemId, registryObject.getId());
                continue;
            }

            log.debug("Generating block model for registry object {} with texture of {}", registryObject.getId(), itemId);
            slab(registryObject.get(), textureBlock);
        }
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block));
    }

    @NonExtendable
    protected final String name(Block block) {
        return rl(block).getPath();
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
     * @param block      the target {@link Block}
     * @param renderType the render type
     */
    protected void defaultBlock(Block block, RenderType renderType) {
        defaultBlock(block, block, renderType);
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block        the target {@link Block}
     * @param textureBlock the texture providing {@link Block}
     */
    protected void defaultBlock(Block block, Block textureBlock) {
        defaultBlock(block, textureBlock, RenderType.DEFAULT);
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block        the target {@link Block}
     * @param textureBlock the texture providing {@link Block}
     * @param renderType   the render type
     */
    protected void defaultBlock(Block block, Block textureBlock, RenderType renderType) {
        getVariantBuilder(block).forAllStates(state -> {
            if (RenderType.DEFAULT.equals(renderType)) {
                return ConfiguredModel.builder()
                    .modelFile(cubeAll(textureBlock))
                    .build();
            } else {
                return ConfiguredModel.builder()
                    .modelFile(models()
                        .cubeAll(name(textureBlock), blockTexture(textureBlock))
                        .renderType(renderType.getId()))
                    .build();
            }
        });

        itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath())
            .parent(new UncheckedModelFile(modLoc("block/" + name(block))));
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param stairBlock   the {@link StairBlock} Object
     * @param textureBlock the {@link Block} you want to use as texture
     */
    protected void stairs(Block stairBlock, Block textureBlock) {
        if (!(stairBlock instanceof StairBlock block)) {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(stairBlock)) + " is not a instance of StairBlock.");
        } else {
            stairsBlock(block, new ResourceLocation(Objects.requireNonNull(rl(textureBlock)).getNamespace(), "block/" + name(textureBlock)));
            itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name(block))));
        }
    }

    protected void stairs(Block stairBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        if (stairBlock instanceof StairBlock block) {
            String baseName = rl(block).toString();
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

            this.itemModels().getBuilder(name(block)).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(stairBlock)) + " is not a instance of StairBlock.");
        }
    }

    protected void sidedStairs(Block stairBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        if (stairBlock instanceof StairBlock block) {
            String baseName = rl(block).toString();
            ModelFile stairs = grassLikeStair(baseName, top, bottom, side);
            ModelFile stairsInner = grassLikeInnerStair(baseName + "_inner", top, bottom, side);
            ModelFile stairsOuter = grassLikeOuterStair(baseName + "_outer", top, bottom, side);

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

            this.itemModels().getBuilder(name(block)).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(stairBlock)) + " is not a instance of StairBlock.");
        }
    }

    protected void stairs(Block stairBlock, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        if (stairBlock instanceof StairBlock block) {
            stairsBlock(block, side, bottom, top);
            this.itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(stairBlock)) + " is not a instance of StairBlock.");
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
            throw new IllegalArgumentException(Objects.requireNonNull(rl(slabBlock)) + " is not a instance of StairBlock.");
        } else {
            ResourceLocation textureLocation = new ResourceLocation(Objects.requireNonNull(rl(textureBlock)).getNamespace(), "block/" + name(textureBlock));
            slabBlock(block, textureLocation, textureLocation);
            itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath()).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name(block))));
        }
    }

    protected void slab(Block slabBlock, Block fullBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation overlay) {
        if (slabBlock instanceof SlabBlock block) {
            ModelFile doubleSlab = models().getExistingFile(new ResourceLocation(rl(fullBlock).getNamespace(), "block/" + name(fullBlock)));
            ModelFile bottomSlab = overlaySlab(name(block), top, bottom, side, overlay);
            ModelFile topSlab = overlaySlabTop(name(block) + "_top", top, bottom, side, overlay);

            getVariantBuilder(block)
                .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottomSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(topSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleSlab));

            this.itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(slabBlock)) + " is not a instance of StairBlock.");
        }
    }

    protected void slab(Block slabBlock, Block fullBlock, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        if (slabBlock instanceof SlabBlock block) {
            slabBlock(block, new ResourceLocation(rl(fullBlock).getNamespace(), "block/" + name(fullBlock)), side, bottom, top);
            this.itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(slabBlock)) + " is not a instance of SlabBlock.");
        }
    }

    protected void slab(Block stairBlock, Block fullBlock, ResourceLocation side, ResourceLocation top) {
        slab(stairBlock, fullBlock, side, top, top);
    }

    protected void sidedSlab(Block slabBlock, Block fullBlock, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        if (slabBlock instanceof SlabBlock block) {
            ModelFile doubleSlab = models().getExistingFile(new ResourceLocation(rl(fullBlock).getNamespace(), "block/" + name(fullBlock)));
            ModelFile bottomSlab = grassLikeSlab(name(block), top, bottom, side);
            ModelFile topSlab = grassLikeSlabTop(name(block) + "_top", top, bottom, side);

            getVariantBuilder(block)
                .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottomSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(topSlab))
                .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleSlab));

            this.itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath()).parent(new ModelFile.UncheckedModelFile(this.modLoc("block/" + name(block))));
        } else {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(slabBlock)) + " is not a instance of StairBlock.");
        }
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param block the {@link RotatedPillarBlock} Object.
     */
    protected void pillar(Block block) {
        if (!(block instanceof RotatedPillarBlock rotatedPillarBlock)) {
            throw new IllegalArgumentException(Objects.requireNonNull(rl(block)) + " is not a instance of RotatedPillarBlock.");
        } else {
            logBlock(rotatedPillarBlock);
            itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name(block))));
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
        itemModels().getBuilder(Objects.requireNonNull(rl(block)).getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name(block))));
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param sapling sapling block
     */
    protected void sapling(Supplier<? extends Block> sapling) {
        sapling(sapling.get());
    }

    /**
     * Generates blockstate, block and item model json file.
     *
     * @param sapling sapling block
     */
    protected void sapling(Block sapling) {
        getVariantBuilder(sapling).forAllStates(blockState -> ConfiguredModel.builder().modelFile(models().cross(name(sapling), blockTexture(sapling))).build());
        itemModels().getBuilder(Objects.requireNonNull(rl(sapling)).getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + name(sapling))));
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

    private BlockModelBuilder grassLikeStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/grass_like_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top);
    }

    private BlockModelBuilder grassLikeInnerStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/grass_like_inner_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top);
    }

    private BlockModelBuilder grassLikeOuterStair(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/grass_like_outer_stairs"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top);
    }

    private BlockModelBuilder grassLikeSlab(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/grass_like_slab"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top);
    }

    private BlockModelBuilder grassLikeSlabTop(String baseName, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        return models().withExistingParent(baseName, new ResourceLocation(ManasCore.MOD_ID, "block/grass_like_slab_top"))
            .texture("side", side)
            .texture("bottom", bottom)
            .texture("top", top);
    }
}

