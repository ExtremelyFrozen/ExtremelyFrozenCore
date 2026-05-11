package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.content.SerializerFluidIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.CustomFluidMapIngredient;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FluidRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public static final FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, true, 1, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(SizedFluidIngredient content) {
        return new SizedFluidIngredient(content.ingredient(), content.amount());
    }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) {
        return new SizedFluidIngredient(content.ingredient(), modifier.apply(content.amount()));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedFluidIngredient ingredient) {
                boolean equal = false;
                for (Object object : list) {
                    if (object instanceof SizedFluidIngredient existing && ingredient.equals(existing)) {
                        equal = true;
                        break;
                    }
                    if (object instanceof FluidStack stack && ingredient.ingredient().test(stack)) {
                        equal = true;
                        break;
                    }
                }
                if (!equal) {
                    list.add(ingredient);
                }
            } else if (item instanceof FluidStack stack) {
                boolean equal = false;
                for (Object object : list) {
                    if (object instanceof SizedFluidIngredient ingredient && ingredient.ingredient().test(stack)) {
                        equal = true;
                        break;
                    }
                    if (object instanceof FluidStack existing && FluidStack.isSameFluidSameComponents(stack, existing)) {
                        equal = true;
                        break;
                    }
                }
                if (!equal) {
                    list.add(stack);
                }
            }
        }
        return list;
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        if (object instanceof FluidIngredient ingredient) {
            return CustomFluidMapIngredient.from(ingredient);
        }
        if (object instanceof SizedFluidIngredient sized) {
            return getDefaultMapIngredient(sized.ingredient());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public boolean shouldBypassDistinct() {
        return false;
    }
}
