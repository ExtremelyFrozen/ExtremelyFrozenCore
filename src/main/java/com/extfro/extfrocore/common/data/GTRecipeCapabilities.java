package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.*;
import com.extfro.extfrocore.api.recipe.ingredient.EnergyStack;
import com.extfro.extfrocore.api.registry.GTRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class GTRecipeCapabilities {

    public final static RecipeCapability<SizedIngredient> ITEM = ItemRecipeCapability.CAP;
    public final static RecipeCapability<SizedFluidIngredient> FLUID = FluidRecipeCapability.CAP;
    public final static RecipeCapability<BlockState> BLOCK_STATE = BlockStateRecipeCapability.CAP;
    public final static RecipeCapability<EnergyStack> EU = EURecipeCapability.CAP;
    public final static RecipeCapability<Integer> CWU = CWURecipeCapability.CAP;

    public static void init() {
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ExtForCore.id(ITEM.name), ITEM);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ExtForCore.id(FLUID.name), FLUID);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ExtForCore.id(BLOCK_STATE.name), BLOCK_STATE);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ExtForCore.id(EU.name), EU);
        GTRegistries.register(GTRegistries.RECIPE_CAPABILITIES, ExtForCore.id(CWU.name), CWU);
    }
}
