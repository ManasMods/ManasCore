package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
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
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {
    public RecipeProvider(final GatherDataEvent gatherDataEvent) {
        super(gatherDataEvent.getGenerator());
    }

    protected abstract void generate(Consumer<FinishedRecipe> pFinishedRecipeConsumer);

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        generate(pFinishedRecipeConsumer);
    }

    protected void allSmeltingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int smeltingTicks, int campfireTicks, int smokingTicks) {
        smeltingRecipe(pFinishedRecipeConsumer, ingredient, result, exp, smeltingTicks);
        campfireRecipe(pFinishedRecipeConsumer, ingredient, result, exp, campfireTicks);
        smokingRecipe(pFinishedRecipeConsumer, ingredient, result, exp, smokingTicks);
    }

    protected void smeltingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            SimpleCookingRecipeBuilder.smelting(ingredient, result, exp, cookingTicks)
                .unlockedBy("has_" + Objects.requireNonNull(itemStack.getItem().getRegistryName()).getPath(), has(itemStack.getItem()))
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

    protected void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, TagKey<Item> tag) {
        RecipeBuilder builder = slabBuilder(slab, Ingredient.of(tag));
        builder.unlockedBy("has_" + tag.location().getNamespace(), has(tag));
        builder.save(finishedRecipeConsumer);
    }

    protected void slab(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, Block... material) {
        RecipeBuilder builder = slabBuilder(slab, Ingredient.of(material));
        for (Block block : material) {
            //noinspection ConstantConditions
            builder.unlockedBy("has_" + block.getRegistryName().getNamespace(), has(block));
        }

        builder.save(finishedRecipeConsumer);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stairs, TagKey<Item> tag) {
        stairs(finishedRecipeConsumer, true, stairs, tag);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, boolean craft8, Block stairs, TagKey<Item> tag) {
        RecipeBuilder builder = craft8 ? betterStairBuilder(stairs, Ingredient.of(tag)) : stairBuilder(stairs, Ingredient.of(tag));
        builder.unlockedBy("has_" + tag.location().getNamespace(), has(tag));
        builder.save(finishedRecipeConsumer);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stairs, Block... material) {
        stairs(finishedRecipeConsumer, true, stairs, material);
    }

    protected void stairs(Consumer<FinishedRecipe> finishedRecipeConsumer, boolean craft8, Block stairs, Block... material) {
        RecipeBuilder builder = craft8 ? betterStairBuilder(stairs, Ingredient.of(material)) : stairBuilder(stairs, Ingredient.of(material));

        for (Block block : material) {
            //noinspection ConstantConditions
            builder.unlockedBy("has_" + block.getRegistryName().getNamespace(), has(block));
        }

        builder.save(finishedRecipeConsumer);
    }

    protected static RecipeBuilder betterStairBuilder(ItemLike pStairs, Ingredient pMaterial) {
        return ShapedRecipeBuilder.shaped(pStairs, 8)
            .define('#', pMaterial)
            .pattern("#  ")
            .pattern("## ")
            .pattern("###");
    }

    @SuppressWarnings("ConstantConditions")
    protected void stairsToBlock(Consumer<FinishedRecipe> finishedRecipeConsumer, Block stair, Block block) {
        ShapelessRecipeBuilder.shapeless(block, 3)
            .requires(stair, 4)
            .unlockedBy("has_" + stair.getRegistryName().getNamespace(), has(stair))
            .save(finishedRecipeConsumer, new ResourceLocation(block.getRegistryName().getNamespace(), "stairs_to_block_" + block.getRegistryName().getPath()));
    }

    @SuppressWarnings("ConstantConditions")
    protected void slabsToBlock(Consumer<FinishedRecipe> finishedRecipeConsumer, Block slab, Block block) {
        ShapelessRecipeBuilder.shapeless(block, 1)
            .requires(slab, 2)
            .unlockedBy("has_" + slab.getRegistryName().getNamespace(), has(slab))
            .save(finishedRecipeConsumer, new ResourceLocation(block.getRegistryName().getNamespace(), "slabs_to_block_" + block.getRegistryName().getPath()));
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
        ShapedRecipeBuilder.shaped(sword)
            .pattern("X")
            .pattern("X")
            .pattern("S")
            .define('X', material)
            .define('S', stick)
            .save(finishedRecipeConsumer);
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
        ShapedRecipeBuilder.shaped(axe)
            .pattern("XX")
            .pattern("XS")
            .pattern(" S")
            .define('X', material)
            .define('S', stick)
            .save(finishedRecipeConsumer);
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
        ShapedRecipeBuilder.shaped(hoe)
            .pattern("XX")
            .pattern(" S")
            .pattern(" S")
            .define('X', material)
            .define('S', stick)
            .save(finishedRecipeConsumer);
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
        ShapedRecipeBuilder.shaped(pickaxe)
            .pattern("XXX")
            .pattern(" S")
            .pattern(" S")
            .define('X', material)
            .define('S', stick)
            .save(finishedRecipeConsumer);
    }
}