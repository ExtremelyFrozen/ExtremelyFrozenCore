package com.extfro.extfrocore.integration.emi.orevein;

import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.integration.xei.widgets.GTOreVeinWidget;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockFluid extends ModularUIEMIRecipe {

    private final Holder<BedrockFluidDefinition> fluid;

    public GTBedrockFluid(Holder<BedrockFluidDefinition> fluid) {
        super(recipe -> ModularUI.of(UI.of(new GTOreVeinWidget(((GTBedrockFluid) recipe).fluid, null))));
        this.fluid = fluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockFluidEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return fluid.getKey().location().withPrefix("/bedrock_fluid_diagram/");
    }

    @Override
    public int getDisplayWidth() {
        return GTOreVeinWidget.width;
    }

    @Override
    public int getDisplayHeight() {
        return 140;
    }
}
