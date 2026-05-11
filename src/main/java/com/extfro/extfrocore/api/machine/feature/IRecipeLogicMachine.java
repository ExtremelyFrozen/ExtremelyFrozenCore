package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.modifier.ModifierFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLogicMachine extends IRecipeCapabilityHolder, IMachineFeature, IWorkable, IVoidable {

    @NotNull
    MachineRecipeType[] getRecipeTypes();

    @NotNull
    MachineRecipeType getRecipeType();

    int getActiveRecipeType();

    void setActiveRecipeType(int type);

    default void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {}

    @NotNull
    RecipeLogic getRecipeLogic();

    default MachineRecipe fullModifyRecipe(MachineRecipe recipe) {
        MachineRecipe trimmed = RecipeHelper.trimRecipeOutputs(recipe, self().getDefinition().getRecipeOutputLimits());
        MachineRecipe modified = doModifyRecipe(trimmed);
        if (modified == null) return null;
        ModifierFunction modifier = self().getDefinition().getRecipeModifier().getModifier(self(), modified);
        MachineRecipe result = modifier.apply(modified);
        if (result == null) {
            RecipeLogic.putFailureReason(getRecipeLogic(), recipe, modifier.getFailReason());
        }
        return result;
    }

    @Nullable
    default MachineRecipe doModifyRecipe(MachineRecipe recipe) {
        return recipe;
    }

    default boolean keepSubscribing() {
        return true;
    }

    default boolean isRecipeLogicAvailable() {
        return true;
    }

    default boolean beforeWorking(@Nullable MachineRecipe recipe) {
        return self().getDefinition().getBeforeWorking().test(this, recipe);
    }

    default boolean onWorking() {
        return self().getDefinition().getOnWorking().test(this);
    }

    default void onWaiting() {
        self().getDefinition().getOnWaiting().accept(this);
    }

    default void afterWorking() {
        self().getDefinition().getAfterWorking().accept(this);
    }

    default boolean regressWhenWaiting() {
        return self().getDefinition().isRegressWhenWaiting();
    }

    default boolean alwaysTryModifyRecipe() {
        return true;
    }

    default boolean shouldWorkingPlaySound() {
        return true;
    }

    default long getDisplayRecipeValue() {
        return -1;
    }

    @Override
    default boolean isWorkingEnabled() {
        return getRecipeLogic().isWorkingEnabled();
    }

    @Override
    default void setWorkingEnabled(boolean isWorkingAllowed) {
        getRecipeLogic().setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    default void setSuspendAfterFinish(boolean suspendAfterFinish) {
        getRecipeLogic().setSuspendAfterFinish(suspendAfterFinish);
    }

    @Override
    default boolean isSuspendAfterFinish() {
        return getRecipeLogic().isSuspendAfterFinish();
    }

    @Override
    default int getProgress() {
        return getRecipeLogic().getProgress();
    }

    @Override
    default int getMaxProgress() {
        return getRecipeLogic().getMaxProgress();
    }

    @Override
    default boolean isActive() {
        return getRecipeLogic().isActive();
    }

    @Override
    MetaMachine self();
}
