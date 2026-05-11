package com.extfro.extfrocore.api.recipe.modifier;

import com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class ParallelLogic {

    private ParallelLogic() {}

    public static int getParallelAmount(MetaMachine machine, MachineRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 1) return Math.max(0, parallelLimit);
        if (!(machine instanceof IRecipeLogicMachine recipeMachine)) return 1;
        int maxInputMultiplier = getMaxByInput(recipeMachine, recipe, parallelLimit, Collections.emptyList());
        if (maxInputMultiplier == 0) return 0;
        return limitByOutputMerging(recipeMachine, recipe, maxInputMultiplier,
                recipeMachine::canVoidRecipeOutputs, Collections.emptyList());
    }

    public static int getMaxByInput(IRecipeCapabilityHolder holder, MachineRecipe recipe, int parallelLimit,
                                    List<RecipeCapability<?>> capsToSkip) {
        int minimum = Integer.MAX_VALUE;
        for (RecipeCapability<?> capability : recipe.inputs.keySet()) {
            if (capability.doMatchInRecipe() && !capsToSkip.contains(capability)) {
                int capParallel = capability.getMaxParallelByInput(holder, recipe, parallelLimit, false);
                if (capParallel == 0) {
                    Component reason = Component.translatable("extfrocore.recipe_logic.insufficient_in")
                            .append(Component.literal(": "))
                            .append(capability.getName());
                    RecipeLogic.putFailureReason(holder, recipe, reason);
                    return 0;
                }
                minimum = Math.min(minimum, capParallel);
            }
        }
        for (RecipeCapability<?> capability : recipe.tickInputs.keySet()) {
            if (capability.doMatchInRecipe() && !capsToSkip.contains(capability)) {
                int capParallel = capability.getMaxParallelByInput(holder, recipe, parallelLimit, true);
                if (capParallel == 0) {
                    Component reason = Component.translatable("extfrocore.recipe_logic.insufficient_in")
                            .append(Component.literal(": "))
                            .append(capability.getName());
                    RecipeLogic.putFailureReason(holder, recipe, reason);
                    return 0;
                }
                minimum = Math.min(minimum, capParallel);
            }
        }
        if (minimum == Integer.MAX_VALUE) {
            Component reason = Component.translatable("extfrocore.recipe_logic.no_capabilities")
                    .append(Component.literal(": "))
                    .append(Component.translatable("extfrocore.recipe_logic.io.in"));
            RecipeLogic.putFailureReason(holder, recipe, reason);
            return 0;
        }
        return minimum;
    }

    public static int limitByOutputMerging(IRecipeCapabilityHolder holder, MachineRecipe recipe, int parallelLimit,
                                           Predicate<RecipeCapability<?>> canVoid,
                                           List<RecipeCapability<?>> capsToSkip) {
        int max = parallelLimit;
        for (RecipeCapability<?> capability : recipe.outputs.keySet()) {
            if (canVoid.test(capability) || !capability.doMatchInRecipe() || capsToSkip.contains(capability)) continue;
            if (!recipe.getOutputContents(capability).isEmpty()) {
                int limit = capability.limitMaxParallelByOutput(holder, recipe, parallelLimit, false);
                if (limit == 0) {
                    Component reason = Component.translatable("extfrocore.recipe_logic.insufficient_out")
                            .append(Component.literal(": "))
                            .append(capability.getName());
                    RecipeLogic.putFailureReason(holder, recipe, reason);
                    return 0;
                }
                max = Math.min(max, limit);
            }
        }
        for (RecipeCapability<?> capability : recipe.tickOutputs.keySet()) {
            if (canVoid.test(capability) || !capability.doMatchInRecipe() || capsToSkip.contains(capability)) continue;
            if (!recipe.getTickOutputContents(capability).isEmpty()) {
                int limit = capability.limitMaxParallelByOutput(holder, recipe, parallelLimit, true);
                if (limit == 0) {
                    Component reason = Component.translatable("extfrocore.recipe_logic.insufficient_out")
                            .append(Component.literal(": "))
                            .append(capability.getName());
                    RecipeLogic.putFailureReason(holder, recipe, reason);
                    return 0;
                }
                max = Math.min(max, limit);
            }
        }
        return max;
    }

    public static int[] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
        if (mergedAll) {
            minMultiplier = multiplier;
            int remainder = (maxMultiplier - multiplier) % 2;
            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
        } else {
            maxMultiplier = multiplier;
            multiplier = (multiplier + minMultiplier) / 2;
        }
        if (maxMultiplier - minMultiplier <= 1) {
            multiplier = maxMultiplier = minMultiplier;
        }
        return new int[] { minMultiplier, multiplier, maxMultiplier };
    }

    public static int getParallelAmountFast(MetaMachine machine, @NotNull MachineRecipe recipe, int parallelLimit) {
        if (parallelLimit <= 1) return Math.max(0, parallelLimit);
        if (!(machine instanceof IRecipeCapabilityHolder holder)) return 1;
        while (parallelLimit > 0) {
            MachineRecipe copied = recipe.copy(ContentModifier.multiplier(parallelLimit), false);
            if (RecipeHelper.matchRecipe(holder, copied).isSuccess() &&
                    RecipeHelper.matchTickRecipe(holder, copied).isSuccess()) {
                return parallelLimit;
            }
            parallelLimit /= 2;
        }
        return 1;
    }
}
