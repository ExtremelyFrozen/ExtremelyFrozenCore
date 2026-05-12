package com.extfro.extfrocore.api.recipe.ingredient;

import com.extfro.extfrocore.api.EFValues;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

import org.jetbrains.annotations.NotNull;

public interface IRangedIngredient {

    IntProvider getCountProvider();

    int getSampledCount();

    void setSampledCount(int count);

    /**
     * If this ingredient has not yet had its count rolled, rolls it and returns the roll.
     * If it has, returns the existing roll.
     * Passthrough method, invokes {@code rollSampledCount()} using the threadsafe {@link EFValues#RNG}.
     *
     * @return the amount rolled
     */
    default int rollSampledCount() {
        return rollSampledCount(EFValues.RNG);
    }

    int rollSampledCount(@NotNull RandomSource random);

    /**
     * @return the average roll of this ranged amount
     */
    default double getMidRoll() {
        return ((getCountProvider().getMaxValue() + getCountProvider().getMinValue()) / 2.0);
    }

    default boolean isRolled() {
        return getSampledCount() != -1;
    }

    void reset();
}
