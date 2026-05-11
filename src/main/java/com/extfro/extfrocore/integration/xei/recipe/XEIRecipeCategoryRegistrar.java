package com.extfro.extfrocore.integration.xei.recipe;

import com.extfro.extfrocore.api.recipe.category.RecipeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class XEIRecipeCategoryRegistrar {

    private static final List<Consumer<RecipeCategory>> CATEGORY_REGISTRARS = new ArrayList<>();
    private static final List<Consumer<XEIRecipeDisplay>> DISPLAY_REGISTRARS = new ArrayList<>();

    private XEIRecipeCategoryRegistrar() {}

    public static void registerCategoryRegistrar(Consumer<RecipeCategory> registrar) {
        CATEGORY_REGISTRARS.add(registrar);
    }

    public static void registerDisplayRegistrar(Consumer<XEIRecipeDisplay> registrar) {
        DISPLAY_REGISTRARS.add(registrar);
    }

    public static void registerCategories() {
        for (RecipeCategory category : XEIRecipeRegistry.getVisibleCategories()) {
            for (Consumer<RecipeCategory> registrar : CATEGORY_REGISTRARS) {
                registrar.accept(category);
            }
        }
    }

    public static void registerDisplays() {
        for (XEIRecipeDisplay display : XEIRecipeRegistry.getAllDisplays()) {
            for (Consumer<XEIRecipeDisplay> registrar : DISPLAY_REGISTRARS) {
                registrar.accept(display);
            }
        }
    }
}
