/*
 * Copyright (c) 2022. ManasMods
 */

package com.github.manasmods.manascore.api.data.gen;

import com.github.manasmods.manascore.core.ShapedRecipeBuilderAccessor;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@AvailableSince("1.0.0.0")
@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {
    public RecipeProvider(final GatherDataEvent gatherDataEvent) {
        super(gatherDataEvent.getGenerator().getPackOutput());
    }

    @OverrideOnly
    protected abstract void generate(Consumer<FinishedRecipe> pWriter);

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        generate(pWriter);
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(ItemStack item) {
        return rl(item.getItem());
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(ItemLike itemLike) {
        return rl(itemLike.asItem());
    }

    @AvailableSince("2.0.0.0")
    @NonExtendable
    protected final ResourceLocation rl(Block block) {
        return rl(block.asItem());
    }

    protected void allSmeltingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, RecipeCategory recipeCategory, ItemLike result, float exp, int smeltingTicks, int campfireTicks, int smokingTicks) {
        smeltingRecipe(pFinishedRecipeConsumer, ingredient, recipeCategory, result, exp, smeltingTicks);
        campfireRecipe(pFinishedRecipeConsumer, ingredient, result, exp, campfireTicks);
        smokingRecipe(pFinishedRecipeConsumer, ingredient, result, exp, smokingTicks);
    }

    protected void smeltingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, RecipeCategory recipeCategory, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            SimpleCookingRecipeBuilder.smelting(ingredient, recipeCategory, result, exp, cookingTicks)
                .unlockedBy("has_" + getHasName(itemStack), has(itemStack.getItem()))
                .save(pFinishedRecipeConsumer, getSmeltingRecipeName(result));
        }
    }

    protected void campfireRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            simpleCookingRecipe(pFinishedRecipeConsumer, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, cookingTicks, itemStack.getItem(), result, exp);
        }
    }

    protected void smokingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            simpleCookingRecipe(pFinishedRecipeConsumer, "smoking", RecipeSerializer.SMOKING_RECIPE, cookingTicks, itemStack.getItem(), result, exp);
        }
    }

    protected void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<Block> slab, TagKey<Item> tag) {
        slab(finishedRecipeConsumer, slab.get(), tag);
    }

    protected void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, TagKey<Item> tag) {
        RecipeBuilder builder = slabBuilder(RecipeCategory.BUILDING_BLOCKS, slab, Ingredient.of(tag));
        builder.unlockedBy("has_" + tag.location().getNamespace(), has(tag));
        builder.save(finishedRecipeConsumer);
    }

    @SafeVarargs
    protected final void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<Block> slab, Supplier<Block>... material) {
        slab(finishedRecipeConsumer, slab.get(), Arrays.stream(material).map(Supplier::get).toArray(Block[]::new));
    }

    protected void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, Block... material) {
        RecipeBuilder builder = slabBuilder(RecipeCategory.BUILDING_BLOCKS, slab, Ingredient.of(material));
        for (Block block : material) {
            builder.unlockedBy("has_" + getHasName(block), has(block));
        }

        builder.save(finishedRecipeConsumer);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<Block> stairs, TagKey<Item> tag) {
        stairs(finishedRecipeConsumer, stairs.get(), tag);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stairs, TagKey<Item> tag) {
        stairs(finishedRecipeConsumer, true, stairs, tag);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, boolean craft8, Block stairs, TagKey<Item> tag) {
        RecipeBuilder builder = craft8 ? betterStairBuilder(stairs, Ingredient.of(tag)) : stairBuilder(stairs, Ingredient.of(tag));
        builder.unlockedBy("has_" + tag.location().getNamespace(), has(tag));
        builder.save(finishedRecipeConsumer);
    }

    @SafeVarargs
    protected final void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<Block> stairs, Supplier<Block>... material) {
        stairs(finishedRecipeConsumer, stairs.get(), Arrays.stream(material).map(Supplier::get).toArray(Block[]::new));
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stairs, Block... material) {
        stairs(finishedRecipeConsumer, true, stairs, material);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, boolean craft8, Block stairs, Block... material) {
        RecipeBuilder builder = craft8 ? betterStairBuilder(stairs, Ingredient.of(material)) : stairBuilder(stairs, Ingredient.of(material));

        for (Block block : material) {
            builder.unlockedBy("has_" + getHasName(block), has(block));
        }

        builder.save(finishedRecipeConsumer);
    }

    protected static RecipeBuilder betterStairBuilder(ItemLike pStairs, Ingredient pMaterial) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pStairs, 8)
            .define('#', pMaterial)
            .pattern("#  ")
            .pattern("## ")
            .pattern("###");
    }

    @SuppressWarnings("ConstantConditions")
    protected void stairsToBlock(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stair, Block block) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block, 3)
            .requires(stair, 4)
            .unlockedBy("has_" + getHasName(stair), has(stair))
            .save(finishedRecipeConsumer, new ResourceLocation(rl(block).getNamespace(), "stairs_to_block/" + rl(block).getPath()));
    }

    @SuppressWarnings("ConstantConditions")
    protected void slabsToBlock(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, Block block) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block, 1)
            .requires(slab, 2)
            .unlockedBy("has_" + getHasName(slab), has(slab))
            .save(finishedRecipeConsumer, new ResourceLocation(rl(block).getNamespace(), "slabs_to_block/" + rl(block).getPath()));
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike sword) {
        sword(finishedRecipeConsumer, Ingredient.of(material), sword);
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike sword) {
        sword(finishedRecipeConsumer, Ingredient.of(material), sword);
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike sword) {
        sword(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), sword);
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike sword) {
        sword(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), sword);
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike sword) {
        sword(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), sword);
    }

    protected void sword(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike sword) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, sword)
            .pattern("X")
            .pattern("X")
            .pattern("S")
            .define('X', material)
            .define('S', stick);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike axe) {
        axe(finishedRecipeConsumer, Ingredient.of(material), axe);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike axe) {
        axe(finishedRecipeConsumer, Ingredient.of(material), axe);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike axe) {
        axe(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), axe);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike axe) {
        axe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), axe);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike axe) {
        axe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), axe);
    }

    protected void axe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike axe) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, axe)
            .pattern("XX")
            .pattern("XS")
            .pattern(" S")
            .define('X', material)
            .define('S', stick);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike hoe) {
        hoe(finishedRecipeConsumer, Ingredient.of(material), hoe);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike hoe) {
        hoe(finishedRecipeConsumer, Ingredient.of(material), hoe);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike hoe) {
        hoe(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), hoe);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike hoe) {
        hoe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), hoe);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike hoe) {
        hoe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), hoe);
    }

    protected void hoe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike hoe) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hoe)
            .pattern("XX")
            .pattern(" S")
            .pattern(" S")
            .define('X', material)
            .define('S', stick);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike pickaxe) {
        pickaxe(finishedRecipeConsumer, Ingredient.of(material), pickaxe);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike pickaxe) {
        pickaxe(finishedRecipeConsumer, Ingredient.of(material), pickaxe);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike pickaxe) {
        pickaxe(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), pickaxe);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike pickaxe) {
        pickaxe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), pickaxe);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike pickaxe) {
        pickaxe(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), pickaxe);
    }

    protected void pickaxe(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike pickaxe) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, pickaxe)
            .pattern("XXX")
            .pattern(" S ")
            .pattern(" S ")
            .define('X', material)
            .define('S', stick);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike shovel) {
        shovel(finishedRecipeConsumer, Ingredient.of(material), shovel);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike shovel) {
        shovel(finishedRecipeConsumer, Ingredient.of(material), shovel);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike shovel) {
        shovel(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), shovel);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike shovel) {
        shovel(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), shovel);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike shovel) {
        shovel(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), shovel);
    }

    protected void shovel(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike shovel) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, shovel)
            .pattern("X")
            .pattern("S")
            .pattern("S")
            .define('X', material)
            .define('S', stick);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        tools(finishedRecipeConsumer, Ingredient.of(material), axe, hoe, pickaxe, shovel, sword);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        tools(finishedRecipeConsumer, Ingredient.of(material), axe, hoe, pickaxe, shovel, sword);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        tools(finishedRecipeConsumer, material, Ingredient.of(Tags.Items.RODS_WOODEN), axe, hoe, pickaxe, shovel, sword);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike stick, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        tools(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), axe, hoe, pickaxe, shovel, sword);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, TagKey<Item> stick, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        tools(finishedRecipeConsumer, Ingredient.of(material), Ingredient.of(stick), axe, hoe, pickaxe, shovel, sword);
    }

    protected void tools(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, Ingredient stick, ItemLike axe, ItemLike hoe, ItemLike pickaxe, ItemLike shovel, ItemLike sword) {
        axe(finishedRecipeConsumer, material, stick, axe);
        hoe(finishedRecipeConsumer, material, stick, hoe);
        pickaxe(finishedRecipeConsumer, material, stick, pickaxe);
        shovel(finishedRecipeConsumer, material, stick, shovel);
        sword(finishedRecipeConsumer, material, stick, sword);
    }


    protected void saveWithAutoUnlock(Consumer<FinishedRecipe> finishedRecipeConsumer, ShapedRecipeBuilder builder, Ingredient material) {
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material, "");
    }

    protected void saveWithAutoUnlock(Consumer<FinishedRecipe> finishedRecipeConsumer, ShapedRecipeBuilder builder, Ingredient material, String filePath) {
        if (material.getItems().length > 1) {
            ((ShapedRecipeBuilderAccessor) builder).getAdvancement().requirements(RequirementsStrategy.OR);
        }

        for (ItemStack stack : material.getItems()) {
            builder.unlockedBy(getHasName(stack.getItem()), has(stack.getItem()));
        }

        ResourceLocation recipeResourceLocation = RecipeBuilder.getDefaultRecipeId(builder.getResult());
        builder.save(finishedRecipeConsumer, new ResourceLocation(recipeResourceLocation.getNamespace(), filePath + recipeResourceLocation.getPath()));
    }

    protected String getHasName(ItemStack stack) {
        return getHasName(stack.getItem());
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<ItemLike> material, ItemLike packedMaterial) {
        nineStorage(finishedRecipeConsumer, material.get(), packedMaterial);
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, Supplier<ItemLike> packedMaterial) {
        nineStorage(finishedRecipeConsumer, material, packedMaterial.get());
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, Supplier<ItemLike> material, Supplier<ItemLike> packedMaterial) {
        nineStorage(finishedRecipeConsumer, material.get(), packedMaterial.get());
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike packedMaterial) {
        nineStorage(finishedRecipeConsumer, Ingredient.of(material), packedMaterial);
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike packedMaterial) {
        nineStorage(finishedRecipeConsumer, Ingredient.of(material), packedMaterial);
    }

    protected void nineStorage(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike packedMaterial) {
        if (material.getItems().length == 0) {
            throw new IllegalStateException("No Item in material ingredient of recipe: " + rl(packedMaterial));
        }
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, packedMaterial)
            .pattern("XXX")
            .pattern("XXX")
            .pattern("XXX")
            .define('X', material);
        saveWithAutoUnlock(finishedRecipeConsumer, builder, material, "storage/pack/");
    }

    protected void helmet(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike helmet) {
        helmet(finishedRecipeConsumer, Ingredient.of(material), helmet);
    }

    protected void helmet(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike helmet) {
        helmet(finishedRecipeConsumer, Ingredient.of(material), helmet);
    }

    protected void helmet(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike helmet) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, helmet)
            .pattern("XXX")
            .pattern("X X")
            .define('X', material);

        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void chestplate(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike chestplate) {
        chestplate(finishedRecipeConsumer, Ingredient.of(material), chestplate);
    }

    protected void chestplate(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike chestplate) {
        chestplate(finishedRecipeConsumer, Ingredient.of(material), chestplate);
    }

    protected void chestplate(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike chestplate) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, chestplate)
            .pattern("X X")
            .pattern("XXX")
            .pattern("XXX")
            .define('X', material);

        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void leggings(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike leggings) {
        leggings(finishedRecipeConsumer, Ingredient.of(material), leggings);
    }

    protected void leggings(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike leggings) {
        leggings(finishedRecipeConsumer, Ingredient.of(material), leggings);
    }

    protected void leggings(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike leggings) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, leggings)
            .pattern("XXX")
            .pattern("X X")
            .pattern("X X")
            .define('X', material);

        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void boots(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike boots) {
        boots(finishedRecipeConsumer, Ingredient.of(material), boots);
    }

    protected void boots(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike boots) {
        boots(finishedRecipeConsumer, Ingredient.of(material), boots);
    }

    protected void boots(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike boots) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, boots)
            .pattern("X X")
            .pattern("X X")
            .define('X', material);

        saveWithAutoUnlock(finishedRecipeConsumer, builder, material);
    }

    protected void armour(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armour(finishedRecipeConsumer, Ingredient.of(material), helmet, chestplate, leggings, boots);
    }

    protected void armour(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armour(finishedRecipeConsumer, Ingredient.of(material), helmet, chestplate, leggings, boots);
    }

    protected void armour(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        helmet(finishedRecipeConsumer, material, helmet);
        chestplate(finishedRecipeConsumer, material, chestplate);
        leggings(finishedRecipeConsumer, material, leggings);
        boots(finishedRecipeConsumer, material, boots);
    }

    protected void armor(Consumer<FinishedRecipe> finishedRecipeConsumer, ItemLike material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armour(finishedRecipeConsumer, Ingredient.of(material), helmet, chestplate, leggings, boots);
    }

    protected void armor(Consumer<FinishedRecipe> finishedRecipeConsumer, TagKey<Item> material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armour(finishedRecipeConsumer, Ingredient.of(material), helmet, chestplate, leggings, boots);
    }

    protected void armor(Consumer<FinishedRecipe> finishedRecipeConsumer, Ingredient material, ItemLike helmet, ItemLike chestplate, ItemLike leggings, ItemLike boots) {
        armour(finishedRecipeConsumer, material, helmet, chestplate, leggings, boots);
    }

    protected void planksFromLogs(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Supplier<ItemLike> pPlanks, TagKey<Item> pLogs) {
        planksFromLogs(pFinishedRecipeConsumer, pPlanks.get(), pLogs, 4);
    }
}