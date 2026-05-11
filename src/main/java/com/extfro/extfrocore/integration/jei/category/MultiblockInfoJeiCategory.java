package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.multipage.XEIMultiblockInfoRegistry;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;

public final class MultiblockInfoJeiCategory extends AbstractXEIJeiCategory<MultiblockInfoDisplay> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 96;

    public MultiblockInfoJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper, XEIMultiblockInfoRegistry.CATEGORY_TITLE, net.minecraft.world.item.ItemStack.EMPTY, WIDTH,
                HEIGHT);
    }

    @Override
    public RecipeType<MultiblockInfoDisplay> getRecipeType() {
        return XEIJeiRecipeTypes.MULTIBLOCK_INFO;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockInfoDisplay recipe, IFocusGroup focuses) {
        var icon = recipe.icon();
        if (!icon.isEmpty()) {
            builder.addOutputSlot(8, 24)
                    .setOutputSlotBackground()
                    .addItemStack(icon);
        }
    }

    @Override
    public void draw(MultiblockInfoDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        drawHeader(guiGraphics, recipe.title());
        drawLine(guiGraphics, Component.literal(recipe.id().toString()), 8, 50);
        drawLine(guiGraphics, Component.literal("Pages: " + recipe.pages().size()), 8, 62);
    }
}
