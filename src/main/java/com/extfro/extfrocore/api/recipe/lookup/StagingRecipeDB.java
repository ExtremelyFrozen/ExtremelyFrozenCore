package com.extfro.extfrocore.api.recipe.lookup;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.MapIngredientTypeManager;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class StagingRecipeDB {

    private final @NotNull ObjectOpenHashSet<MachineRecipe> recipes = new ObjectOpenHashSet<>();

    public boolean add(@NotNull MachineRecipe recipe) {
        return recipes.add(recipe);
    }

    public void clear() {
        recipes.clear();
        recipes.trim();
    }

    public void populateDB(@NotNull RecipeDB db) {
        Object2IntMap<Object> frequencies = inputFrequencies();
        for (MachineRecipe recipe : recipes) {
            List<Pair<RecipeCapability<?>, Object>> flatContent = flattenedContent(recipe);
            flatContent.sort(Comparator.comparingInt(entry -> frequencies.getInt(entry.right())));
            List<List<AbstractMapIngredient>> inputs = new ArrayList<>(flatContent.size());
            for (Pair<RecipeCapability<?>, Object> entry : flatContent) {
                List<AbstractMapIngredient> ingredients = MapIngredientTypeManager.getFrom(entry.right(), entry.left());
                MapIngredientPool.applyPooling(ingredients);
                inputs.add(ingredients);
            }
            if (inputs.isEmpty()) {
                recipe.recipeCategory.addRecipe(recipe);
            } else if (!db.add(recipe, inputs)) {
                ExtForCore.LOGGER.warn("Failed to add recipe from staging into lookup DB: {}",
                        recipe.getRecipeLocation());
            }
        }
    }

    private @NotNull Object2IntMap<Object> inputFrequencies() {
        Object2IntMap<Object> map = new Object2IntOpenHashMap<>();
        for (MachineRecipe recipe : recipes) {
            recipe.inputs.forEach((capability, list) -> {
                for (Object input : compressedContent(list, capability)) {
                    map.mergeInt(input, 1, Integer::sum);
                }
            });
            recipe.tickInputs.forEach((capability, list) -> {
                for (Object input : compressedContent(list, capability)) {
                    map.mergeInt(input, 1, Integer::sum);
                }
            });
        }
        return map;
    }

    private static @NotNull List<Object> compressedContent(@NotNull List<Content> list,
                                                           @NotNull RecipeCapability<?> capability) {
        return capability.compressIngredients(list.stream().map(Content::getContent).toList());
    }

    private static @NotNull List<Pair<RecipeCapability<?>, Object>> flattenedContent(@NotNull MachineRecipe recipe) {
        Map<RecipeCapability<?>, List<Content>> map = new Object2ObjectOpenHashMap<>();
        recipe.inputs.forEach((capability, list) -> buildInputsByCapability(map, capability, list));
        recipe.tickInputs.forEach((capability, list) -> buildInputsByCapability(map, capability, list));
        List<Pair<RecipeCapability<?>, Object>> list = new ArrayList<>();
        map.forEach((capability, contents) -> {
            for (Content content : contents) {
                list.add(Pair.of(capability, content.getContent()));
            }
        });
        return list;
    }

    private static void buildInputsByCapability(@NotNull Map<RecipeCapability<?>, List<Content>> map,
                                                @NotNull RecipeCapability<?> capability,
                                                @NotNull List<Content> list) {
        if (!capability.isRecipeSearchFilter()) {
            return;
        }
        map.compute(capability, (key, value) -> {
            if (value == null) {
                return new ArrayList<>(list);
            }
            value.addAll(list);
            return value;
        });
    }
}
