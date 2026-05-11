package com.extfro.extfrocore.api.recipe.modifier;

import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.api.recipe.MachineRecipe;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class EfficiencyModifier implements RecipeModifier {

    private final double baseMultiplier;
    private final double efficiency;
    private final double hardCap;
    private final double heuristic;

    private EfficiencyModifier(double baseMultiplier, double efficiency, double hardCap) {
        Preconditions.checkArgument(baseMultiplier > 0, "Base multiplier must be > 0: %s", baseMultiplier);
        Preconditions.checkArgument(efficiency > 0, "Efficiency must be > 0: %s", efficiency);
        Preconditions.checkArgument(hardCap >= 0, "Hard cap must be >= 0: %s", hardCap);
        this.baseMultiplier = baseMultiplier;
        this.efficiency = efficiency;
        this.hardCap = hardCap;
        this.heuristic = 300 * efficiency * efficiency;
    }

    public static EfficiencyModifier of(double baseMultiplier, double efficiency, double hardCap) {
        return new EfficiencyModifier(baseMultiplier, efficiency, hardCap);
    }

    public static EfficiencyModifier of(double baseMultiplier, double efficiency) {
        return of(baseMultiplier, efficiency, 0.5);
    }

    public static EfficiencyModifier of(double efficiency) {
        return of(2, efficiency, 0.5);
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull MachineRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine recipeMachine)) {
            return RecipeModifier.nullWrongType(IRecipeLogicMachine.class, machine);
        }
        if (recipe.duration <= 1) return ModifierFunction.IDENTITY;
        int runs = recipeMachine.getRecipeLogic().getConsecutiveRecipes();
        double multiplier = runs > heuristic ? hardCap : Math.max(hardCap, baseMultiplier * Math.pow(efficiency, runs));
        return ModifierFunction.builder().durationMultiplier(multiplier).build();
    }
}
