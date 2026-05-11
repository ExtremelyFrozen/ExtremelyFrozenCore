package com.extfro.extfrocore.api.recipe.lookup;

import com.extfro.extfrocore.api.recipe.lookup.ingredient.AbstractMapIngredient;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@ApiStatus.Internal
public final class MapIngredientPool {

    private static final Map<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> POOL = new WeakHashMap<>();

    private MapIngredientPool() {}

    static void applyPooling(@NotNull List<AbstractMapIngredient> list) {
        for (int i = 0; i < list.size(); i++) {
            AbstractMapIngredient ingredient = list.get(i);
            WeakReference<AbstractMapIngredient> pooledReference = POOL.get(ingredient);
            if (pooledReference == null) {
                POOL.put(ingredient, new WeakReference<>(ingredient));
                continue;
            }
            AbstractMapIngredient pooled = pooledReference.get();
            if (pooled == null) {
                POOL.put(ingredient, new WeakReference<>(ingredient));
            } else {
                list.set(i, pooled);
            }
        }
    }

    public static void clear() {
        POOL.clear();
    }
}
