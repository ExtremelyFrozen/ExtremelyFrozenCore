package com.extfro.extfrocore.api.recipe.modifier;

import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.MachineRecipe;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class RecipeModifierList implements RecipeModifier {

    @Getter
    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    @Contract(pure = true)
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull MachineRecipe recipe) {
        ModifierFunction result = ModifierFunction.IDENTITY;
        MachineRecipe runningRecipe = recipe;
        for (RecipeModifier modifier : modifiers) {
            ModifierFunction function = modifier.getModifier(machine, runningRecipe);
            runningRecipe = function.apply(runningRecipe);
            if (runningRecipe == null) {
                RecipeLogic.putFailureReason(machine, recipe, function.getFailReason());
                return ModifierFunction.NULL;
            }
            result = function.compose(result);
        }
        return result;
    }
}
