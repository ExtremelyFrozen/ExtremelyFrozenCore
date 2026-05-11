package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.api.recipe.MachineRecipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public interface IRecipeHandler<K> extends IFilteredHandler<K> {

    Comparator<IRecipeHandler<?>> ENTRY_COMPARATOR = (first, second) -> {
        int priority = IFilteredHandler.PRIORITY_COMPARATOR.compare(first, second);
        if (priority != 0) return priority;
        boolean firstEmpty = first.getTotalContentAmount() <= 0;
        boolean secondEmpty = second.getTotalContentAmount() <= 0;
        return Boolean.compare(firstEmpty, secondEmpty);
    };

    List<K> handleRecipeInner(IO io, MachineRecipe recipe, List<K> left, boolean simulate);

    default int getSize() {
        return -1;
    }

    @NotNull
    List<Object> getContents();

    double getTotalContentAmount();

    default boolean isDistinct() {
        return false;
    }

    default boolean shouldSearchContent() {
        return true;
    }

    RecipeCapability<K> getCapability();

    @SuppressWarnings("unchecked")
    default K copyContent(Object content) {
        return getCapability().copyInner((K) content);
    }

    default List<K> handleRecipe(IO io, MachineRecipe recipe, List<?> left, boolean simulate) {
        List<K> contents = new ObjectArrayList<>(left.size());
        for (Object object : left) {
            contents.add(copyContent(object));
        }
        return handleRecipeInner(io, recipe, contents, simulate);
    }
}
