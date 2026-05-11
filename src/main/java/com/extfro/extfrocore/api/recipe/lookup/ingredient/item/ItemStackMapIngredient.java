package com.extfro.extfrocore.api.recipe.lookup.ingredient.item;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ItemStackMapIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient;

    public ItemStackMapIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStackMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        for (Ingredient.Value value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue(ItemStack item)) {
                ingredients.add(new ItemStackMapIngredient(item, ingredient));
            }
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new ItemStackMapIngredient(stack));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            ItemStackMapIngredient other = (ItemStackMapIngredient) obj;
            if (!ItemStack.isSameItem(stack, other.stack)) {
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
        return stack.getItem().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "ItemStackMapIngredient{item=" + stack + "}";
    }
}
