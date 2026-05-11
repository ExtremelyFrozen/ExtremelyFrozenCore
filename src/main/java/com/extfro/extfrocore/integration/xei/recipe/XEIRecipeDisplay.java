package com.extfro.extfrocore.integration.xei.recipe;

import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public record XEIRecipeDisplay(RecipeCategory category, MachineRecipe recipe) {

    @Nullable
    public ResourceLocation id() {
        return recipe.getRecipeLocation();
    }
}
