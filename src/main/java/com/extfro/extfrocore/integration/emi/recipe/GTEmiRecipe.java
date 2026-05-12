package com.extfro.extfrocore.integration.emi.recipe;

import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.integration.xei.widgets.GTRecipeElement;

import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTEmiRecipe extends ModularUIEMIRecipe {

    final EmiRecipeCategory category;
    final GTRecipe recipe;

    public GTEmiRecipe(GTRecipe recipe, EmiRecipeCategory category) {
        super(emiRecipe -> ModularUI.of(UI.of(new GTRecipeElement(((GTEmiRecipe) emiRecipe).recipe))));
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public int getDisplayWidth() {
        return recipe.recipeType.getRecipeUI().getJEISize().width;
    }

    @Override
    public int getDisplayHeight() {
        return recipe.recipeType.getRecipeUI().getJEISize().height;
    }
}
