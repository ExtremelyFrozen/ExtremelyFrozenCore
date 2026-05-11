package com.extfro.extfrocore.integration.xei;

import com.extfro.extfrocore.integration.emi.EMIRegistrars;
import com.extfro.extfrocore.integration.emi.category.EmiXEIRegistrars;
import com.extfro.extfrocore.integration.jei.JEIRegistrars;
import com.extfro.extfrocore.integration.jei.category.XEIJeiCategories;

public final class EFXEIRegistration {

    private static boolean initialized;

    private EFXEIRegistration() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        XEIPages.init();
        registerJEI();
        registerEMI();
    }

    private static void registerJEI() {
        JEIRegistrars.registerCategoryRegistrar(XEIJeiCategories::registerCategories);
        JEIRegistrars.registerRecipeRegistrar(XEIJeiCategories::registerRecipes);
        JEIRegistrars.registerCatalystRegistrar(XEIJeiCategories::registerRecipeCatalysts);
    }

    private static void registerEMI() {
        EMIRegistrars.registerRegistrar(EmiXEIRegistrars::register);
    }
}
