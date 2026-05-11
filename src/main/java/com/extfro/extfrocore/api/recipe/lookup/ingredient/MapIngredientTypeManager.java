package com.extfro.extfrocore.api.recipe.lookup.ingredient;

import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.CustomFluidMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidDataComponentMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.fluid.FluidTagMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.CustomItemMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.ItemDataComponentMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.extfro.extfrocore.api.recipe.lookup.ingredient.item.ItemTagMapIngredient;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class MapIngredientTypeManager {

    private static final Map<Class<?>, List<? extends MapIngredientFunction<?>>> INGREDIENT_FUNCTIONS =
            new ConcurrentHashMap<>(7);
    private static final Map<MapIngredientFunction<?>, Class<?>> INGREDIENT_TYPES = new ConcurrentHashMap<>(7);

    private MapIngredientTypeManager() {}

    static {
        registerMapIngredient(Ingredient.class, ingredient -> {
            List<AbstractMapIngredient> list = new ArrayList<>();
            list.addAll(ItemTagMapIngredient.from(ingredient));
            list.addAll(ItemStackMapIngredient.from(ingredient));
            if (ingredient.getCustomIngredient() instanceof DataComponentIngredient componentIngredient) {
                list.addAll(ItemDataComponentMapIngredient.from(componentIngredient));
            }
            list.addAll(CustomItemMapIngredient.from(ingredient));
            return list;
        });
        registerMapIngredient(ItemStack.class, stack -> {
            List<AbstractMapIngredient> list = new ArrayList<>();
            list.addAll(ItemTagMapIngredient.from(stack));
            list.addAll(ItemStackMapIngredient.from(stack));
            list.addAll(ItemDataComponentMapIngredient.from(stack));
            list.addAll(CustomItemMapIngredient.from(stack));
            return list;
        });
        registerMapIngredient(DataComponentIngredient.class, ItemDataComponentMapIngredient::from);
        registerMapIngredient(SingleFluidIngredient.class, FluidStackMapIngredient::from);
        registerMapIngredient(TagFluidIngredient.class, FluidTagMapIngredient::from);
        registerMapIngredient(DataComponentFluidIngredient.class, FluidDataComponentMapIngredient::from);
        registerMapIngredient(FluidStack.class, stack -> {
            List<AbstractMapIngredient> list = new ArrayList<>();
            list.addAll(FluidTagMapIngredient.from(stack));
            list.addAll(FluidStackMapIngredient.from(stack));
            list.addAll(FluidDataComponentMapIngredient.from(stack));
            list.addAll(CustomFluidMapIngredient.from(stack));
            return list;
        });
    }

    public static <T> void registerMapIngredient(Class<T> ingredientClass,
                                                 MapIngredientFunction<T> function) {
        ingredientClass = boxClass(ingredientClass);
        List<MapIngredientFunction<T>> list = (List<MapIngredientFunction<T>>) INGREDIENT_FUNCTIONS
                .computeIfAbsent(ingredientClass, ignored -> new ArrayList<>());
        list.add(function);
        INGREDIENT_TYPES.put(function, ingredientClass);
    }

    @NotNull
    public static <T> List<AbstractMapIngredient> getFrom(T object, RecipeCapability<?> capability) {
        Class<? super T> objClass = (Class<? super T>) boxClass(object.getClass());
        Class<?> stopAt = boxClass(capability.serializer.contentClass());
        if (!stopAt.isAssignableFrom(objClass)) {
            stopAt = Object.class;
        }
        List<? extends MapIngredientFunction<? super T>> functions = getTypesForClass(objClass, stopAt);
        if (functions.isEmpty()) {
            return Objects.requireNonNullElseGet(capability.getDefaultMapIngredient(object), Collections::emptyList);
        }
        if (!objClass.isAssignableFrom(stopAt)) {
            List<AbstractMapIngredient> defaults = getDefaultIngredients(object, capability, stopAt, functions);
            if (defaults != null) {
                return defaults;
            }
        }

        List<AbstractMapIngredient> values = new ArrayList<>();
        for (MapIngredientFunction<? super T> function : functions) {
            values.addAll(function.getIngredients(object));
        }
        return values;
    }

    private static <T> List<? extends MapIngredientFunction<? super T>> getTypesForClass(Class<T> objClass,
                                                                                         Class<?> stopAt) {
        Preconditions.checkArgument(stopAt.isAssignableFrom(objClass),
                "stopAt must be a superclass of %s", objClass);

        List<? extends MapIngredientFunction<?>> types = INGREDIENT_FUNCTIONS.get(objClass);
        if (types == null && objClass != stopAt) {
            Class<? super T> superclass = objClass.getSuperclass();
            if (superclass == null || superclass == stopAt) {
                return Collections.emptyList();
            }
            return getTypesForClass(superclass, stopAt);
        }
        return types == null ? Collections.emptyList() : (List<MapIngredientFunction<T>>) types;
    }

    private static <T> @Nullable List<AbstractMapIngredient> getDefaultIngredients(
                                                                                   T object,
                                                                                   RecipeCapability<?> capability,
                                                                                   Class<?> stopAt,
                                                                                   List<? extends MapIngredientFunction<? super T>> functions) {
        for (MapIngredientFunction<? super T> function : functions) {
            if (INGREDIENT_TYPES.get(function) != stopAt) {
                return null;
            }
        }
        return Objects.requireNonNullElseGet(capability.getDefaultMapIngredient(object), Collections::emptyList);
    }

    private static final Map<Class<?>, Class<?>> WRAPPERS = Util.make(new HashMap<>(9), map -> {
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(double.class, Double.class);
        map.put(float.class, Float.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(short.class, Short.class);
        map.put(void.class, Void.class);
    });

    private static <T> @NotNull Class<T> boxClass(Class<T> clazz) {
        return (Class<T>) WRAPPERS.getOrDefault(clazz, clazz);
    }
}
