package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.content.SerializerIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.CustomItemMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemRecipeCapability extends RecipeCapability<SizedIngredient> {

    public static final ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, true, 0, SerializerIngredient.INSTANCE);
    }

    @Override
    public SizedIngredient copyInner(SizedIngredient content) {
        return content;
    }

    @Override
    public SizedIngredient copyWithModifier(SizedIngredient content, ContentModifier modifier) {
        return new SizedIngredient(content.ingredient(), modifier.apply(content.count()));
    }

    @Override
    public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof SizedIngredient ingredient) {
                boolean equal = false;
                for (Object object : list) {
                    if (object instanceof SizedIngredient existing && ingredient.ingredient().equals(existing.ingredient())) {
                        equal = true;
                        break;
                    }
                    if (object instanceof ItemStack stack && ingredient.ingredient().test(stack)) {
                        equal = true;
                        break;
                    }
                }
                if (!equal) {
                    list.add(ingredient);
                }
            } else if (item instanceof ItemStack stack) {
                boolean equal = false;
                for (Object object : list) {
                    if (object instanceof Ingredient ingredient && ingredient.test(stack)) {
                        equal = true;
                        break;
                    }
                    if (object instanceof SizedIngredient ingredient && ingredient.ingredient().test(stack)) {
                        equal = true;
                        break;
                    }
                    if (object instanceof ItemStack existing && ItemStack.isSameItemSameComponents(stack, existing)) {
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
        if (object instanceof Ingredient ingredient) {
            return CustomItemMapIngredient.from(ingredient);
        }
        if (object instanceof SizedIngredient sized) {
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
