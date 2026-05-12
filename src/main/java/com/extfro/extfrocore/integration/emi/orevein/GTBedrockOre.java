package com.extfro.extfrocore.integration.emi.orevein;

import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.integration.xei.widgets.GTOreVeinWidget;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockOre extends ModularUIEMIRecipe {

    private final Holder<BedrockOreDefinition> bedrockOre;

    public GTBedrockOre(Holder<BedrockOreDefinition> bedrockOre) {
        super(recipe -> ModularUI.of(UI.of(new GTOreVeinWidget(((GTBedrockOre) recipe).bedrockOre, null))));
        this.bedrockOre = bedrockOre;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockOreEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return bedrockOre.getKey().location().withPrefix("/bedrock_ore_diagram/");
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
