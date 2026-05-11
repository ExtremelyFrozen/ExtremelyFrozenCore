package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplayRegistry;
import com.extfro.extfrocore.integration.xei.multipage.XEIMultiblockInfoRegistry;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinRegistry;

import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

public final class XEIJeiCategories {

    private XEIJeiCategories() {}

    public static void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new OreVeinJeiCategory(guiHelper));
        for (var category : OreProcessingRegistry.getCategories()) {
            registration.addRecipeCategories(new OreProcessingJeiCategory(guiHelper, category));
        }
        registration.addRecipeCategories(new CircuitJeiCategory(guiHelper), new MultiblockInfoJeiCategory(guiHelper));
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(XEIJeiRecipeTypes.ORE_VEIN, XEIOreVeinRegistry.getAllDisplays());
        for (var category : OreProcessingRegistry.getCategories()) {
            registration.addRecipes(XEIJeiRecipeTypes.oreProcessing(category), OreProcessingRegistry.getDisplays(category));
        }
        registration.addRecipes(XEIJeiRecipeTypes.CIRCUIT, CircuitDisplayRegistry.getDisplays());
        registration.addRecipes(XEIJeiRecipeTypes.MULTIBLOCK_INFO, XEIMultiblockInfoRegistry.getAllDisplays());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var category : OreProcessingRegistry.getCategories()) {
            for (var catalyst : category.catalysts()) {
                if (!catalyst.isEmpty()) {
                    registration.addRecipeCatalyst(catalyst, XEIJeiRecipeTypes.oreProcessing(category));
                }
            }
        }
        for (var display : CircuitDisplayRegistry.getDisplays()) {
            var output = display.firstOutput();
            if (output != null && !output.isEmpty()) {
                registration.addRecipeCatalyst(output, XEIJeiRecipeTypes.CIRCUIT);
            }
        }
        for (var display : XEIMultiblockInfoRegistry.getAllDisplays()) {
            var icon = display.icon();
            if (!icon.isEmpty()) {
                registration.addRecipeCatalyst(icon, XEIJeiRecipeTypes.MULTIBLOCK_INFO);
            }
        }
    }
}
