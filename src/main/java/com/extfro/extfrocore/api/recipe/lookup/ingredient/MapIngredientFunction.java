package com.extfro.extfrocore.api.recipe.lookup.ingredient;

import java.util.List;

@FunctionalInterface
public interface MapIngredientFunction<T> {

    List<AbstractMapIngredient> getIngredients(T ingredient);
}
