package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.machine.MetaMachine;

import org.jetbrains.annotations.Nullable;

public interface RecipeLogicContext {

    RecipeLogicContext EMPTY = new RecipeLogicContext() {};

    @Nullable
    default MetaMachine machine() {
        return null;
    }

    default int recipeTier() {
        return 0;
    }

    default int chanceTier() {
        return recipeTier();
    }
}
