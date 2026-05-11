package com.extfro.extfrocore.api.recipe.modifier;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.recipe.MachineRecipe;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RecipeModifier {

    RecipeModifier NO_MODIFIER = (machine, recipe) -> ModifierFunction.IDENTITY;

    @Contract(pure = true)
    @NotNull
    ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull MachineRecipe recipe);

    @Contract(pure = true)
    default @Nullable MachineRecipe applyModifier(@NotNull MetaMachine machine, @NotNull MachineRecipe recipe) {
        return getModifier(machine, recipe).apply(recipe);
    }

    static ModifierFunction nullWrongType(Class<?> type, MetaMachine actual) {
        ExtForCore.LOGGER.error("Incorrect use of recipe modifier, expected machine of type {}, received {}",
                type.getSimpleName(), actual.getDefinition().getName());
        return ModifierFunction.NULL;
    }
}
