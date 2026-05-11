package com.extfro.extfrocore.api.recipe.lookup;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.RecipeHelper;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.MapIngredientTypeManager;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class RecipeDB {

    private final @NotNull Branch rootBranch = new Branch();

    @ApiStatus.Internal
    public void clear() {
        rootBranch.clear();
    }

    public Stream<MachineRecipe> getRecipes() {
        return rootBranch.getRecipes();
    }

    public @Nullable MachineRecipe find(@NotNull IRecipeCapabilityHolder holder) {
        return find(holder, recipe -> RecipeHelper.matchRecipe(holder, recipe).isSuccess());
    }

    public @Nullable MachineRecipe find(@NotNull IRecipeCapabilityHolder holder,
                                        @NotNull Predicate<MachineRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = fromHolder(holder);
        if (list == null) {
            return null;
        }
        return find(list, predicate);
    }

    @ApiStatus.Internal
    @VisibleForTesting
    public @Nullable MachineRecipe find(@NotNull List<List<AbstractMapIngredient>> list,
                                        @NotNull Predicate<MachineRecipe> predicate) {
        RecipeIterator iter = new RecipeIterator(this, list, predicate);
        return iter.hasNext() ? iter.next() : null;
    }

    public @Nullable MachineRecipe find(@NotNull Map<RecipeCapability<?>, List<Object>> inputs,
                                        @NotNull Predicate<MachineRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = new ArrayList<>();
        inputs.forEach((capability, contents) -> {
            if (!capability.isRecipeSearchFilter()) {
                return;
            }
            for (Object ingredient : capability.compressIngredients(contents)) {
                list.add(MapIngredientTypeManager.getFrom(ingredient, capability));
            }
        });
        if (list.isEmpty()) {
            return null;
        }
        return find(list, predicate);
    }

    public @Nullable RecipeIterator iterator(@NotNull IRecipeCapabilityHolder holder,
                                             @NotNull Predicate<MachineRecipe> predicate) {
        List<List<AbstractMapIngredient>> list = fromHolder(holder);
        if (list == null) {
            return null;
        }
        return new RecipeIterator(this, list, predicate);
    }

    private @Nullable List<List<AbstractMapIngredient>> fromHolder(@NotNull IRecipeCapabilityHolder holder) {
        Map<RecipeCapability<?>, List<com.extfro.extfrocore.api.capability.recipe.IRecipeHandler<?>>> handlerMap =
                holder.getCapabilitiesFlat().getOrDefault(IO.IN, Collections.emptyMap());
        if (handlerMap.isEmpty()) {
            return null;
        }

        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(handlerMap.size() * 8);
        handlerMap.forEach((capability, handlers) -> {
            if (!capability.isRecipeSearchFilter()) {
                return;
            }
            for (var handler : handlers) {
                for (Object ingredient : capability.compressIngredients(handler.getContents())) {
                    list.add(MapIngredientTypeManager.getFrom(ingredient, capability));
                }
            }
        });
        return list.isEmpty() ? null : list;
    }

    private static @NotNull Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> nodesForIngredient(
                                                                                                          @NotNull AbstractMapIngredient ingredient,
                                                                                                          @NotNull Branch branch) {
        return ingredient.isSpecialIngredient() ? branch.getSpecialNodes() : branch.getNodes();
    }

    boolean add(@NotNull MachineRecipe recipe, @NotNull List<List<AbstractMapIngredient>> ingredients) {
        if (addRecursive(recipe, ingredients, rootBranch, 0)) {
            recipe.recipeCategory.addRecipe(recipe);
            return true;
        }
        return false;
    }

    private boolean addRecursive(@NotNull MachineRecipe recipe,
                                 @NotNull List<List<AbstractMapIngredient>> ingredients,
                                 @NotNull Branch branch, int index) {
        if (index >= ingredients.size()) {
            return true;
        }
        boolean lastIngredient = index == ingredients.size() - 1;
        List<AbstractMapIngredient> current = ingredients.get(index);
        for (AbstractMapIngredient ingredient : current) {
            Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> nodes = nodesForIngredient(ingredient, branch);
            Either<MachineRecipe, Branch> either = nodes.compute(ingredient, (key, value) -> {
                if (lastIngredient) {
                    if (value == null) {
                        return Either.left(recipe);
                    }
                    if (value.left().isEmpty() || !value.left().get().equals(recipe)) {
                        warnConflict(recipe, value);
                    }
                    return value;
                }
                return value == null ? Either.right(new Branch()) : value;
            });

            if (either.left().isPresent()) {
                if (either.left().get() == recipe) {
                    continue;
                }
                return false;
            }
            boolean added = either.right()
                    .filter(child -> addRecursive(recipe, ingredients, child, index + 1))
                    .isPresent();
            if (!added) {
                Either<MachineRecipe, Branch> child = nodes.get(ingredient);
                if (lastIngredient || child != null && child.right().isPresent() && child.right().get().isEmptyBranch()) {
                    nodes.remove(ingredient);
                }
                return false;
            }
        }
        return true;
    }

    private static void warnConflict(@NotNull MachineRecipe recipe, Either<MachineRecipe, Branch> existing) {
        if (!ExtForCore.isDev()) {
            return;
        }
        ExtForCore.LOGGER.warn("Recipe duplicate or conflict found in recipe type {} and was not added",
                BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
        existing.left().ifPresent(conflict -> ExtForCore.LOGGER.warn("Attempted to add recipe {}, which conflicts with {}",
                recipe.getRecipeLocation(), conflict.getRecipeLocation()));
    }

    private static final class SearchFrame {

        int index;
        int ingredientIndex;
        Branch branch;

        private SearchFrame(int index, Branch branch) {
            this.index = index;
            this.branch = branch;
        }
    }

    public static class RecipeIterator implements Iterator<MachineRecipe> {

        private final @NotNull RecipeDB db;
        private final @NotNull List<List<AbstractMapIngredient>> ingredients;
        private final @NotNull Predicate<MachineRecipe> predicate;
        private final Deque<SearchFrame> stack = new ArrayDeque<>();
        private @Nullable MachineRecipe nextCached;
        private boolean hasCached;

        @VisibleForTesting
        public RecipeIterator(@NotNull RecipeDB db,
                              @NotNull List<List<AbstractMapIngredient>> ingredients,
                              @NotNull Predicate<MachineRecipe> predicate) {
            this.db = db;
            this.ingredients = ingredients;
            this.predicate = predicate;
            reset();
        }

        private @Nullable MachineRecipe getNext() {
            while (!stack.isEmpty()) {
                SearchFrame frame = stack.peek();
                if (frame.ingredientIndex >= ingredients.get(frame.index).size()) {
                    stack.pop();
                    continue;
                }

                List<AbstractMapIngredient> ingredientList = ingredients.get(frame.index);
                AbstractMapIngredient ingredient = ingredientList.get(frame.ingredientIndex++);
                Either<MachineRecipe, Branch> result = nodesForIngredient(ingredient, frame.branch).get(ingredient);
                if (result == null) {
                    continue;
                }

                if (result.left().isPresent()) {
                    MachineRecipe recipe = result.left().get();
                    if (predicate.test(recipe)) {
                        return recipe;
                    }
                }

                result.ifRight(branch -> {
                    for (int index = ingredients.size() - 1; index >= 0; index--) {
                        stack.push(new SearchFrame(index, branch));
                    }
                });
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            if (!hasCached) {
                nextCached = getNext();
                hasCached = true;
            }
            return nextCached != null;
        }

        @Override
        public MachineRecipe next() {
            if (!hasCached) {
                nextCached = getNext();
            }
            hasCached = false;
            if (nextCached == null) {
                throw new NoSuchElementException();
            }
            return nextCached;
        }

        public void reset() {
            stack.clear();
            for (int index = ingredients.size() - 1; index >= 0; index--) {
                stack.push(new SearchFrame(index, db.rootBranch));
            }
            hasCached = false;
            nextCached = null;
        }
    }
}
