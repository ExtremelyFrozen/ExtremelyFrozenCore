package com.extfro.extfrocore.integration.xei.recipe;

import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;
import com.extfro.extfrocore.api.registry.EFRegistries;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public final class XEIRecipeRegistry {

    private XEIRecipeRegistry() {}

    @Unmodifiable
    public static List<RecipeCategory> getVisibleCategories() {
        List<RecipeCategory> categories = new ArrayList<>();
        for (RecipeCategory category : EFRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                categories.add(category);
            }
        }
        return List.copyOf(categories);
    }

    @Unmodifiable
    public static List<XEIRecipeDisplay> getDisplays(RecipeCategory category) {
        MachineRecipeType recipeType = category.getRecipeType();
        if (recipeType == null) {
            return List.of();
        }
        List<XEIRecipeDisplay> displays = new ArrayList<>();
        for (MachineRecipe recipe : recipeType.getRecipesInCategory(category)) {
            displays.add(new XEIRecipeDisplay(category, recipe));
        }
        return List.copyOf(displays);
    }

    @Unmodifiable
    public static List<XEIRecipeDisplay> getAllDisplays() {
        List<XEIRecipeDisplay> displays = new ArrayList<>();
        for (RecipeCategory category : getVisibleCategories()) {
            displays.addAll(getDisplays(category));
        }
        return List.copyOf(displays);
    }
}
