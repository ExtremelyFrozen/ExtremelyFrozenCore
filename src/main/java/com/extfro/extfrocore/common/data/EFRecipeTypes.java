package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.recipe.MachineRecipeSerializer;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public final class EFRecipeTypes {

    public static final MachineRecipeType DUMMY_RECIPES = register("dummy", "dummy");

    private EFRecipeTypes() {}

    public static void init() {}

    public static MachineRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        ResourceLocation id = ExtForCore.id(name);
        MachineRecipeType recipeType = new MachineRecipeType(id, group, proxyRecipes);
        EFRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        recipeType.setSerializer(EFRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName,
                MachineRecipeSerializer.create()));
        EFRegistries.register(EFRegistries.RECIPE_TYPES, recipeType.registryName, recipeType);
        return recipeType;
    }
}
