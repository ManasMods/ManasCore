package com.github.manasmods.manascore.core;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipeBuilder.class)
public interface ShapedRecipeBuilderAccessor {
    @Accessor
    Advancement.Builder getAdvancement();
}
