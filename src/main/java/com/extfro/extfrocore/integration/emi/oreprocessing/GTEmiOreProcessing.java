package com.extfro.extfrocore.integration.emi.oreprocessing;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.integration.xei.widgets.GTOreByProductWidget;

import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTEmiOreProcessing extends ModularUIEMIRecipe {

    final Material material;

    public GTEmiOreProcessing(Material material) {
        super(recipe -> ModularUI.of(UI.of(new GTOreByProductWidget(((GTEmiOreProcessing) recipe).material))));
        this.material = material;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return material.getResourceLocation().withPrefix("/");
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return 176;
    }

    @Override
    public int getDisplayHeight() {
        return 166;
    }
}
