package com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomFluidMapIngredient extends AbstractMapIngredient {

    protected FluidStack stack;
    protected FluidIngredient ingredient;

    public CustomFluidMapIngredient(FluidStack stack) {
        this.stack = stack;
    }

    public CustomFluidMapIngredient(FluidStack stack, FluidIngredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(FluidIngredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        for (FluidStack stack : ingredient.getStacks()) {
            ingredients.add(new CustomFluidMapIngredient(stack, ingredient));
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(FluidStack stack) {
        return Collections.singletonList(new CustomFluidMapIngredient(stack));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            CustomFluidMapIngredient other = (CustomFluidMapIngredient) obj;
            if (!FluidStack.isSameFluid(stack, other.stack)) {
                return false;
            }
            if (ingredient != null) {
                return other.ingredient != null ? ingredient.equals(other.ingredient) : ingredient.test(other.stack);
            }
            return other.ingredient == null || other.ingredient.test(stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        return FluidStack.hashFluidAndComponents(stack);
    }

    @Override
    public String toString() {
        return "CustomFluidMapIngredient{fluid=" + stack + ", ingredient=" + ingredient + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
