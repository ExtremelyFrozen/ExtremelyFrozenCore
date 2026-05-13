package com.extfro.extfrocore.integration.jei.circuit;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.integration.xei.widgets.GTProgrammedCircuitWidget;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.NotNull;

public class GTProgrammedCircuitCategory extends ModularUIRecipeCategory<GTProgrammedCircuitWidget> {

    public final static RecipeType<GTProgrammedCircuitWidget> RECIPE_TYPE = new RecipeType<>(
            ExtForCore.id("programmed_circuit"), GTProgrammedCircuitWidget.class);
    private final IDrawable background;
    private final IDrawable icon;

    public GTProgrammedCircuitCategory(IJeiHelpers helpers) {
        super(widget -> ModularUI.of(UI.of(widget)));
        background = helpers.getGuiHelper().createBlankDrawable(150, 80);
        icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
    }

    @Override
    public @NotNull RecipeType<GTProgrammedCircuitWidget> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    @Override
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 80;
    }
}
