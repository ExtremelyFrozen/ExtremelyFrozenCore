package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.recipe.BasicRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.FluidRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.ItemRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.content.SerializerBoolean;
import com.extfro.extfrocore.api.recipe.content.SerializerDouble;
import com.extfro.extfrocore.api.recipe.content.SerializerFloat;
import com.extfro.extfrocore.api.recipe.content.SerializerInteger;
import com.extfro.extfrocore.api.recipe.content.SerializerLong;
import com.extfro.extfrocore.api.registry.EFRegistries;

public final class EFRecipeCapabilities {

    public static final RecipeCapability<Integer> INTEGER = new BasicRecipeCapability<>(
            "integer", 0xFFE0E0E0, false, 0, SerializerInteger.INSTANCE);
    public static final RecipeCapability<Long> LONG = new BasicRecipeCapability<>(
            "long", 0xFFE0E0E0, false, 1, SerializerLong.INSTANCE);
    public static final RecipeCapability<Float> FLOAT = new BasicRecipeCapability<>(
            "float", 0xFFE0E0E0, false, 2, SerializerFloat.INSTANCE);
    public static final RecipeCapability<Double> DOUBLE = new BasicRecipeCapability<>(
            "double", 0xFFE0E0E0, false, 3, SerializerDouble.INSTANCE);
    public static final RecipeCapability<Boolean> BOOLEAN = new BasicRecipeCapability<>(
            "boolean", 0xFFE0E0E0, false, 4, SerializerBoolean.INSTANCE);
    public static final ItemRecipeCapability ITEM = ItemRecipeCapability.CAP;
    public static final FluidRecipeCapability FLUID = FluidRecipeCapability.CAP;

    private EFRecipeCapabilities() {}

    public static void init() {
        register(INTEGER);
        register(LONG);
        register(FLOAT);
        register(DOUBLE);
        register(BOOLEAN);
        register(ITEM);
        register(FLUID);
    }

    private static void register(RecipeCapability<?> capability) {
        EFRegistries.register(EFRegistries.RECIPE_CAPABILITIES, ExtForCore.id(capability.name), capability);
    }
}
