package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingDisplay;
import com.extfro.extfrocore.integration.xei.oreprocessing.OreProcessingRegistry;

import net.minecraft.client.gui.GuiGraphics;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;

public final class OreProcessingJeiCategory extends AbstractXEIJeiCategory<OreProcessingDisplay> {

    public OreProcessingJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper, OreProcessingRegistry.DEFAULT_CATEGORY.title(), OreProcessingRegistry.DEFAULT_CATEGORY.icon(),
                OreProcessingRegistry.DEFAULT_CATEGORY.width(), OreProcessingRegistry.DEFAULT_CATEGORY.height());
    }

    @Override
    public RecipeType<OreProcessingDisplay> getRecipeType() {
        return XEIJeiRecipeTypes.ORE_PROCESSING;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OreProcessingDisplay recipe, IFocusGroup focuses) {
        XEIJeiIngredientHelper.addItemEntries(builder.addInputSlot(8, 28).setStandardSlotBackground(),
                recipe.primaryInput());

        int stepX = 36;
        for (var step : recipe.steps()) {
            int y = 28;
            for (var input : step.itemInputs()) {
                XEIJeiIngredientHelper.addItemEntries(builder.addInputSlot(stepX, y).setStandardSlotBackground(),
                        input);
                y += 20;
            }
            for (var input : step.fluidInputs()) {
                XEIJeiIngredientHelper.addFluidEntries(builder.addInputSlot(stepX, y).setStandardSlotBackground()
                        .setFluidRenderer(1000, false, 16, 16), input);
                y += 20;
            }

            y = 28;
            for (var output : step.itemOutputs()) {
                if (!output.isEmpty()) {
                    builder.addOutputSlot(stepX + 58, y)
                            .setOutputSlotBackground()
                            .addItemStack(output.copy());
                    y += 24;
                }
            }
            for (var output : step.fluidOutputs()) {
                XEIJeiIngredientHelper.addFluidStack(builder.addOutputSlot(stepX + 58, y)
                        .setOutputSlotBackground()
                        .setFluidRenderer(1000, false, 16, 16), output);
                y += 24;
            }
            stepX += 34;
        }
    }

    @Override
    public void draw(OreProcessingDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        drawHeader(guiGraphics, recipe.title());
        int x = 36;
        for (var step : recipe.steps()) {
            drawLine(guiGraphics, step.title(), x, 14);
            x += 34;
        }
    }
}
