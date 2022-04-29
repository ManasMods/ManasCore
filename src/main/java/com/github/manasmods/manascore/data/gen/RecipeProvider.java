package com.github.manasmods.manascore.data.gen;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IConditionBuilder {
    public RecipeProvider(final GatherDataEvent gatherDataEvent) {
        super(gatherDataEvent.getGenerator());
    }

    protected abstract void generate();

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
        generate();
    }

    private void allSmeltingRecipes(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int smeltingTicks, int campfireTicks, int smokingTicks) {
        smeltingRecipe(pFinishedRecipeConsumer, ingredient, result, exp, smeltingTicks);
        campfireRecipe(pFinishedRecipeConsumer, ingredient, result, exp, campfireTicks);
        smokingRecipe(pFinishedRecipeConsumer, ingredient, result, exp, smokingTicks);
    }

    private void smeltingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            SimpleCookingRecipeBuilder.smelting(ingredient, result, exp, cookingTicks)
                .unlockedBy("has_" + Objects.requireNonNull(itemStack.getItem().getRegistryName()).getPath(), has(itemStack.getItem()))
                .save(pFinishedRecipeConsumer);
        }
    }

    private void campfireRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            simpleCookingRecipe(pFinishedRecipeConsumer, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING_RECIPE, cookingTicks, itemStack.getItem(), result, exp);
        }
    }

    private void smokingRecipe(Consumer<FinishedRecipe> pFinishedRecipeConsumer, Ingredient ingredient, ItemLike result, float exp, int cookingTicks) {
        for (ItemStack itemStack : ingredient.getItems()) {
            simpleCookingRecipe(pFinishedRecipeConsumer, "smoking", RecipeSerializer.SMOKING_RECIPE, cookingTicks, itemStack.getItem(), result, exp);
        }
    }
}