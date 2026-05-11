package com.extfro.extfrocore.api.recipe.lookup.ingredient.item;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemDataComponentMapIngredient extends ItemStackMapIngredient {

    protected DataComponentIngredient componentIngredient;

    public ItemDataComponentMapIngredient(ItemStack stack, DataComponentIngredient componentIngredient,
                                          Ingredient vanilla) {
        super(stack, vanilla);
        this.componentIngredient = componentIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentIngredient ingredient) {
        Ingredient vanilla = new Ingredient(ingredient);
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack stack : vanilla.getItems()) {
            list.add(new ItemDataComponentMapIngredient(stack, ingredient, vanilla));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        Ingredient strict = DataComponentIngredient.of(true, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) strict.getCustomIngredient(), strict));

        Ingredient partial = DataComponentIngredient.of(false, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) partial.getCustomIngredient(), partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemDataComponentMapIngredient other) {
            if (!ItemStack.isSameItem(stack, other.stack)) {
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
                        for (ItemStack thisStack : ingredient.getItems()) {
                            for (ItemStack otherStack : other.ingredient.getItems()) {
                                if (ItemStack.isSameItemSameComponents(thisStack, otherStack)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                    boolean thisContains = componentIngredient.items().stream()
                            .allMatch(holder -> other.componentIngredient.items().contains(holder));
                    boolean otherContains = other.componentIngredient.items().stream()
                            .allMatch(holder -> componentIngredient.items().contains(holder));
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
        return "ItemDataComponentMapIngredient{item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
