package com.extfro.extfrocore.api.recipe.lookup.ingredient.item;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomItemMapIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient;

    public CustomItemMapIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public CustomItemMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
            ingredients.add(new CustomItemMapIngredient(stack, ingredient));
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new CustomItemMapIngredient(stack));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            CustomItemMapIngredient other = (CustomItemMapIngredient) obj;
            if (!ItemStack.isSameItem(stack, other.stack)) {
                return false;
            }
            if (ingredient != null) {
                if (other.ingredient != null) {
                    for (ItemStack otherStack : other.ingredient.getItems()) {
                        if (!ingredient.test(otherStack)) return false;
                    }
                    for (ItemStack thisStack : ingredient.getItems()) {
                        if (!other.ingredient.test(thisStack)) return false;
                    }
                    return true;
                }
                return ingredient.test(other.stack);
            }
            return other.ingredient == null || other.ingredient.test(stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        return stack.getItemHolder().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "CustomItemMapIngredient{item=" + stack + ", ingredient=" + ingredient + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
