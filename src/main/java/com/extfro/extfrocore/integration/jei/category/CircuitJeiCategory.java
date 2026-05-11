package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.circuit.CircuitDisplay;
import com.extfro.extfrocore.integration.xei.circuit.CircuitStackEntry;
import com.extfro.extfrocore.integration.xei.circuit.CircuitViewerPage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

public final class CircuitJeiCategory extends AbstractXEIJeiCategory<CircuitDisplay> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 96;

    public CircuitJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper, Component.translatable("extfrocore.xei.circuit"), new ItemStack(Items.REPEATER), WIDTH,
                HEIGHT);
    }

    @Override
    public RecipeType<CircuitDisplay> getRecipeType() {
        return XEIJeiRecipeTypes.CIRCUIT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CircuitDisplay recipe, IFocusGroup focuses) {
        CircuitViewerPage page = CircuitViewerPage.of(recipe);
        for (var slot : page.slots()) {
            CircuitStackEntry entry = slot.entry();
            RecipeIngredientRole role = entry.isInput() && !entry.isOutput() ? RecipeIngredientRole.INPUT :
                    RecipeIngredientRole.OUTPUT;
            IRecipeSlotBuilder slotBuilder = builder.addSlot(role, slot.x(), 24 + slot.y())
                    .setStandardSlotBackground()
                    .addItemStack(entry.stack().copy());
            if (!entry.tooltip().isEmpty()) {
                slotBuilder.addRichTooltipCallback((view, tooltip) -> tooltip.addAll(entry.tooltip()));
            }
        }
    }

    @Override
    public void draw(CircuitDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX,
                     double mouseY) {
        drawHeader(guiGraphics, recipe.title());
    }
}
