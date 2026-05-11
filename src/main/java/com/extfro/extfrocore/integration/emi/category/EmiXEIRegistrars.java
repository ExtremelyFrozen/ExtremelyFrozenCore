package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.integration.xei.XEIPages;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplayRegistry;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoPage;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingCategory;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinRegistry;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;

import java.util.HashMap;
import java.util.Map;

public final class EmiXEIRegistrars {

    public static final EmiRecipeCategory ORE_VEIN = new NamedEmiRecipeCategory(
            XEIPages.ORE_VEIN.id(),
            XEIPages.ORE_VEIN.title(),
            XEIPages.ORE_VEIN.icon());
    public static final EmiRecipeCategory CIRCUIT = new NamedEmiRecipeCategory(
            XEIPages.CIRCUIT.id(),
            XEIPages.CIRCUIT.title(),
            XEIPages.CIRCUIT.icon());
    public static final EmiRecipeCategory MULTIBLOCK_INFO = new NamedEmiRecipeCategory(
            XEIPages.MULTIBLOCK_INFO.id(),
            XEIPages.MULTIBLOCK_INFO.title(),
            XEIPages.MULTIBLOCK_INFO.icon());

    private EmiXEIRegistrars() {}

    public static void register(EmiRegistry registry) {
        registerOreVeins(registry);
        registerOreProcessing(registry);
        registerCircuits(registry);
        registerMultiblockInfo(registry);
    }

    public static void registerOreVeins(EmiRegistry registry) {
        registry.addCategory(ORE_VEIN);
        for (XEIOreVeinDisplay display : XEIOreVeinRegistry.getAllDisplays()) {
            registry.addRecipe(new OreVeinEmiRecipe(ORE_VEIN, display));
        }
    }

    public static void registerOreProcessing(EmiRegistry registry) {
        Map<OreProcessingCategory, EmiRecipeCategory> categories = new HashMap<>();
        for (OreProcessingCategory category : OreProcessingRegistry.getCategories()) {
            EmiRecipeCategory emiCategory = new NamedEmiRecipeCategory(category.id(), category.title(), category.icon());
            categories.put(category, emiCategory);
            registry.addCategory(emiCategory);
            for (var catalyst : category.catalysts()) {
                registry.addWorkstation(emiCategory, EmiXEIHelper.item(catalyst));
            }
        }
        for (OreProcessingDisplay display : OreProcessingRegistry.getDisplays()) {
            EmiRecipeCategory category = categories.get(display.category());
            if (category != null) {
                registry.addRecipe(new OreProcessingEmiRecipe(category, display));
            }
        }
    }

    public static void registerCircuits(EmiRegistry registry) {
        registry.addCategory(CIRCUIT);
        for (CircuitDisplay display : CircuitDisplayRegistry.getDisplays()) {
            registry.addRecipe(new CircuitEmiRecipe(CIRCUIT, display));
        }
    }

    public static void registerMultiblockInfo(EmiRegistry registry) {
        registry.addCategory(MULTIBLOCK_INFO);
        for (MultiblockInfoDisplay display : XEIPages.MULTIBLOCK_INFO.displays()) {
            for (MultiblockInfoPage page : display.pages()) {
                registry.addRecipe(new MultiblockInfoEmiRecipe(MULTIBLOCK_INFO, display, page));
            }
        }
    }
}
