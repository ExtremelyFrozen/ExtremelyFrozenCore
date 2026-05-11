package com.extfro.extfrocore.integration.jei;

import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class JEIRegistrars {

    private static final List<Consumer<IRecipeCategoryRegistration>> CATEGORY_REGISTRARS = new ArrayList<>();
    private static final List<Consumer<IRecipeRegistration>> RECIPE_REGISTRARS = new ArrayList<>();
    private static final List<Consumer<IRecipeCatalystRegistration>> CATALYST_REGISTRARS = new ArrayList<>();

    private JEIRegistrars() {}

    public static void registerCategoryRegistrar(Consumer<IRecipeCategoryRegistration> registrar) {
        CATEGORY_REGISTRARS.add(registrar);
    }

    public static void registerRecipeRegistrar(Consumer<IRecipeRegistration> registrar) {
        RECIPE_REGISTRARS.add(registrar);
    }

    public static void registerCatalystRegistrar(Consumer<IRecipeCatalystRegistration> registrar) {
        CATALYST_REGISTRARS.add(registrar);
    }

    static void registerCategories(IRecipeCategoryRegistration registration) {
        CATEGORY_REGISTRARS.forEach(registrar -> registrar.accept(registration));
    }

    static void registerRecipes(IRecipeRegistration registration) {
        RECIPE_REGISTRARS.forEach(registrar -> registrar.accept(registration));
    }

    static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        CATALYST_REGISTRARS.forEach(registrar -> registrar.accept(registration));
    }
}
