package com.extfro.extfrocore.api.misc;

import com.extfro.extfrocore.api.capability.recipe.EURecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeHandler;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.ingredient.EnergyStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IgnoreEnergyRecipeHandler implements IRecipeHandler<EnergyStack> {

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        return null;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(EnergyStack.MAX);
    }

    @Override
    public double getTotalContentAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public RecipeCapability<EnergyStack> getCapability() {
        return EURecipeCapability.CAP;
    }
}
