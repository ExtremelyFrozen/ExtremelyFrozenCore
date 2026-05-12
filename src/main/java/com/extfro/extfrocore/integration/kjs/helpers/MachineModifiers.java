package com.extfro.extfrocore.integration.kjs.helpers;

import com.extfro.extfrocore.api.machine.SimpleGeneratorMachine;
import com.extfro.extfrocore.api.machine.steam.SimpleSteamMachine;
import com.extfro.extfrocore.api.machine.steam.SteamBoilerMachine;
import com.extfro.extfrocore.api.recipe.modifier.RecipeModifier;
import com.extfro.extfrocore.common.machine.multiblock.electric.FusionReactorMachine;
import com.extfro.extfrocore.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.extfro.extfrocore.common.machine.multiblock.generator.LargeTurbineMachine;
import com.extfro.extfrocore.common.machine.multiblock.steam.LargeBoilerMachine;
import com.extfro.extfrocore.common.machine.multiblock.steam.SteamParallelMultiblockMachine;

/**
 * Collection of Recipe Modifiers for different machines
 * Makes using them for KJS machines easier
 */
@SuppressWarnings("unused")
public final class MachineModifiers {

    public static RecipeModifier SIMPLE_STEAM = SimpleSteamMachine::recipeModifier;
    public static RecipeModifier SIMPLE_GENERATOR = SimpleGeneratorMachine::recipeModifier;
    public static RecipeModifier STEAM_BOILER = SteamBoilerMachine::recipeModifier;
    public static RecipeModifier LARGE_BOILER = LargeBoilerMachine::recipeModifier;
    public static RecipeModifier LARGE_TURBINE = LargeTurbineMachine::recipeModifier;
    public static RecipeModifier LARGE_COMBUSTION_ENGINE = LargeCombustionEngineMachine::recipeModifier;
    public static RecipeModifier STEAM_PARALLEL_MULTIBLOCK = SteamParallelMultiblockMachine::recipeModifier;
    public static RecipeModifier FUSION_REACTOR = FusionReactorMachine::recipeModifier;
}
