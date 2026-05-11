package com.extfro.extfrocore.api.recipe.category;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true)
public class RecipeCategory {

    public static final RecipeCategory DEFAULT = new RecipeCategory(ExtForCore.id("default"));

    public final ResourceLocation registryKey;
    public final String name;
    @Getter
    private final String languageKey;
    @Getter
    private final MachineRecipeType recipeType;
    @Getter
    @Setter
    private boolean XEIVisible = true;

    private RecipeCategory(@NotNull ResourceLocation registryKey) {
        this.registryKey = registryKey;
        this.name = registryKey.getPath();
        this.languageKey = registryKey.toLanguageKey("recipe_category");
        this.recipeType = null;
    }

    public RecipeCategory(@NotNull MachineRecipeType recipeType) {
        this.recipeType = recipeType;
        this.name = recipeType.registryName.getPath();
        this.registryKey = recipeType.registryName;
        this.languageKey = recipeType.getTranslationKey();
    }

    public RecipeCategory(@NotNull ResourceLocation registryKey, @NotNull MachineRecipeType recipeType) {
        this.recipeType = recipeType;
        this.name = registryKey.getPath();
        this.registryKey = registryKey;
        this.languageKey = registryKey.toLanguageKey("recipe_category");
    }

    public static RecipeCategory registerDefault(@NotNull MachineRecipeType recipeType) {
        RecipeCategory category = new RecipeCategory(recipeType);
        EFRegistries.register(EFRegistries.RECIPE_CATEGORIES, category.registryKey, category);
        return category;
    }

    public void addRecipe(MachineRecipe recipe) {
        if (recipeType != null) {
            recipeType.addToCategoryMap(this, recipe);
        }
    }

    public boolean shouldRegisterDisplays() {
        return XEIVisible || ExtForCore.isDev();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecipeCategory that)) return false;
        return registryKey.equals(that.registryKey);
    }

    @Override
    public int hashCode() {
        return registryKey.hashCode();
    }

    @Override
    public String toString() {
        return "RecipeCategory{%s}".formatted(registryKey);
    }
}
