package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;

public interface IVoidable {

    default boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return false;
    }
}
