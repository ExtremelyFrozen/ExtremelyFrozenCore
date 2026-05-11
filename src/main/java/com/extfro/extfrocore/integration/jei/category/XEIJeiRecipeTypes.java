package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingCategory;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;

import mezz.jei.api.recipe.RecipeType;

import java.util.HashMap;
import java.util.Map;

public final class XEIJeiRecipeTypes {

    private static final Map<OreProcessingCategory, RecipeType<OreProcessingDisplay>> ORE_PROCESSING_TYPES =
            new HashMap<>();

    public static final RecipeType<XEIOreVeinDisplay> ORE_VEIN = RecipeType.create(ExtForCore.MOD_ID,
            "ore_vein", XEIOreVeinDisplay.class);
    public static final RecipeType<OreProcessingDisplay> ORE_PROCESSING =
            oreProcessing(OreProcessingRegistry.DEFAULT_CATEGORY);
    public static final RecipeType<CircuitDisplay> CIRCUIT = RecipeType.create(ExtForCore.MOD_ID,
            "circuit", CircuitDisplay.class);
    public static final RecipeType<MultiblockInfoDisplay> MULTIBLOCK_INFO = RecipeType.create(ExtForCore.MOD_ID,
            "multiblock_info", MultiblockInfoDisplay.class);

    private XEIJeiRecipeTypes() {}

    public static RecipeType<OreProcessingDisplay> oreProcessing(OreProcessingCategory category) {
        return ORE_PROCESSING_TYPES.computeIfAbsent(category, key -> RecipeType.create(
                key.id().getNamespace(),
                key.id().getPath(),
                OreProcessingDisplay.class));
    }
}
