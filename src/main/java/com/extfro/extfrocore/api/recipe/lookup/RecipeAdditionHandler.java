package com.extfro.extfrocore.api.recipe.lookup;

import com.extfro.extfrocore.api.recipe.MachineRecipe;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
@RequiredArgsConstructor
public final class RecipeAdditionHandler {

    private final @NotNull StagingRecipeDB stagingDB = new StagingRecipeDB();
    private final @NotNull RecipeDB db;
    private boolean staging;

    public void beginStaging() {
        if (staging) {
            throw new IllegalStateException("cannot begin staging while already in staging state");
        }
        staging = true;
    }

    public void addStaging(@NotNull MachineRecipe recipe) {
        if (!staging) {
            throw new IllegalStateException("cannot add a staging recipe while not in staging state");
        }
        stagingDB.add(recipe);
    }

    public void completeStaging() {
        if (!staging) {
            throw new IllegalStateException("cannot complete staging while not in staging state");
        }
        db.clear();
        stagingDB.populateDB(db);
        stagingDB.clear();
        staging = false;
    }
}
