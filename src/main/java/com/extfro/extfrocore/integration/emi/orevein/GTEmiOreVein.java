package com.extfro.extfrocore.integration.emi.orevein;

import com.extfro.extfrocore.api.data.worldgen.GTOreDefinition;
import com.extfro.extfrocore.integration.xei.widgets.GTOreVeinWidget;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.emi.ModularUIEMIRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTEmiOreVein extends ModularUIEMIRecipe {

    private final Holder<GTOreDefinition> oreDefinition;

    public GTEmiOreVein(Holder<GTOreDefinition> oreDefinition) {
        super(recipe -> ModularUI.of(UI.of(new GTOreVeinWidget(((GTEmiOreVein) recipe).oreDefinition))));
        this.oreDefinition = oreDefinition;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreVeinEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return oreDefinition.getKey().location().withPrefix("/ore_vein_diagram/");
    }

    @Override
    public List<EmiStack> getOutputs() {
        return GTOreVeinWidget.getContainedOresAndBlocks(oreDefinition.value())
                .stream()
                .map(EmiStack::of)
                .toList();
    }

    @Override
    public int getDisplayWidth() {
        return GTOreVeinWidget.width;
    }

    @Override
    public int getDisplayHeight() {
        return 160;
    }
}
