package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplayRegistry;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoPage;
import com.extfro.extfrocore.integration.xei.multipage.XEIMultiblockInfoRegistry;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingCategory;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinRegistry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

import java.util.HashMap;
import java.util.Map;

public final class EmiXEIRegistrars {

    public static final EmiRecipeCategory ORE_VEIN = new NamedEmiRecipeCategory(
            ExtForCore.id("ore_vein"),
            Component.translatable("extfrocore.xei.ore_vein"),
            EmiStack.of(Items.RAW_IRON));
    public static final EmiRecipeCategory CIRCUIT = new NamedEmiRecipeCategory(
            ExtForCore.id("circuit"),
            Component.translatable("extfrocore.xei.circuit"),
            EmiStack.of(Items.COMPARATOR));
    public static final EmiRecipeCategory MULTIBLOCK_INFO = new NamedEmiRecipeCategory(
            XEIMultiblockInfoRegistry.CATEGORY_ID,
            XEIMultiblockInfoRegistry.CATEGORY_TITLE,
            EmiStack.of(Items.STRUCTURE_BLOCK));

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
        for (MultiblockInfoDisplay display : XEIMultiblockInfoRegistry.getAllDisplays()) {
            for (MultiblockInfoPage page : display.pages()) {
                registry.addRecipe(new MultiblockInfoEmiRecipe(MULTIBLOCK_INFO, display, page));
            }
        }
    }
}
