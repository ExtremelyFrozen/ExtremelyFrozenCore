package com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidDataComponentMapIngredient extends FluidStackMapIngredient {

    protected DataComponentFluidIngredient componentIngredient;

    public FluidDataComponentMapIngredient(FluidStack stack, DataComponentFluidIngredient componentIngredient) {
        super(stack.getFluidHolder());
        this.stack = stack;
        this.componentIngredient = componentIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentFluidIngredient ingredient) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (FluidStack stack : ingredient.getStacks()) {
            list.add(new FluidDataComponentMapIngredient(stack, ingredient));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        FluidIngredient strict = DataComponentFluidIngredient.of(true, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) strict));

        FluidIngredient partial = DataComponentFluidIngredient.of(false, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FluidDataComponentMapIngredient other) {
            if (!FluidStack.isSameFluid(stack, other.stack)) {
                return false;
            }
            if (componentIngredient == other.componentIngredient) {
                return true;
            }

            if (componentIngredient != null) {
                if (other.componentIngredient != null) {
                    if (componentIngredient.isStrict() != other.componentIngredient.isStrict()) {
                        return false;
                    }
                    if (!componentIngredient.components().equals(other.componentIngredient.components())) {
                        return false;
                    }
                    if (componentIngredient.isStrict()) {
                        for (FluidStack thisStack : componentIngredient.getStacks()) {
                            for (FluidStack otherStack : other.componentIngredient.getStacks()) {
                                if (FluidStack.isSameFluidSameComponents(thisStack, otherStack)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    boolean thisContains = componentIngredient.fluids().stream()
                            .allMatch(holder -> other.componentIngredient.fluids().contains(holder));
                    boolean otherContains = other.componentIngredient.fluids().stream()
                            .allMatch(holder -> componentIngredient.fluids().contains(holder));
                    return thisContains && otherContains;
                }
                return componentIngredient.test(other.stack);
            }
            return other.componentIngredient.test(stack);
        }
        return false;
    }

    @Override
    public String toString() {
        return "FluidDataComponentMapIngredient{fluid=" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }

    @Override
    protected int hash() {
        return componentIngredient == null ? super.hash() : componentIngredient.hashCode();
    }
}
