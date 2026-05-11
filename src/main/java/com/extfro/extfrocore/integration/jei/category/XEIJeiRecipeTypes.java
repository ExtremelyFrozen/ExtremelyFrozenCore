package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;

import mezz.jei.api.recipe.RecipeType;

public final class XEIJeiRecipeTypes {

    public static final RecipeType<XEIOreVeinDisplay> ORE_VEIN = RecipeType.create(ExtForCore.MOD_ID,
            "ore_vein", XEIOreVeinDisplay.class);
    public static final RecipeType<OreProcessingDisplay> ORE_PROCESSING = RecipeType.create(ExtForCore.MOD_ID,
            "ore_processing", OreProcessingDisplay.class);
    public static final RecipeType<CircuitDisplay> CIRCUIT = RecipeType.create(ExtForCore.MOD_ID,
            "circuit", CircuitDisplay.class);
    public static final RecipeType<MultiblockInfoDisplay> MULTIBLOCK_INFO = RecipeType.create(ExtForCore.MOD_ID,
            "multiblock_info", MultiblockInfoDisplay.class);

    private XEIJeiRecipeTypes() {}
}
