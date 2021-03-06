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
                .save(finishedRecipeConsumer, new ResourceLocation(block.getRegistryName().getNamespace(),"slabs_to_block_" + block.getRegistryName().getPath()));
    }
}