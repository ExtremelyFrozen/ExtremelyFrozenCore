package com.extfro.extfrocore.api.recipe.lookup;

import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

@ApiStatus.Internal
final class Branch {

    private Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> nodes;
    private Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> specialNodes;

    public Stream<MachineRecipe> getRecipes() {
        Stream<MachineRecipe> stream = null;
        if (nodes != null) {
            stream = nodes.values().stream()
                    .flatMap(either -> either.map(Stream::of, Branch::getRecipes));
        }
        if (specialNodes != null) {
            Stream<MachineRecipe> special = specialNodes.values().stream()
                    .flatMap(either -> either.map(Stream::of, Branch::getRecipes));
            stream = stream == null ? special : Stream.concat(stream, special);
        }
        return stream == null ? Stream.empty() : stream;
    }

    public boolean isEmptyBranch() {
        return (nodes == null || nodes.isEmpty()) && (specialNodes == null || specialNodes.isEmpty());
    }

    @NotNull
    public Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> getNodes() {
        if (nodes == null) {
            nodes = new Object2ObjectOpenHashMap<>(2);
        }
        return nodes;
    }

    @NotNull
    public Map<AbstractMapIngredient, Either<MachineRecipe, Branch>> getSpecialNodes() {
        if (specialNodes == null) {
            specialNodes = new Object2ObjectOpenHashMap<>(2);
        }
        return specialNodes;
    }

    public void clear() {
        specialNodes = null;
        nodes = null;
    }
}
