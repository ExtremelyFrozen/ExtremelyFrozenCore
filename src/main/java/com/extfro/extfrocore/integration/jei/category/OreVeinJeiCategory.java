package com.extfro.extfrocore.integration.jei.category;

import com.extfro.extfrocore.integration.xei.orevein.XEIOreVeinDisplay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;

public final class OreVeinJeiCategory extends AbstractXEIJeiCategory<XEIOreVeinDisplay> {

    private static final Component TITLE = Component.translatable("extfrocore.xei.ore_vein");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 96;

    public OreVeinJeiCategory(IGuiHelper guiHelper) {
        super(guiHelper, TITLE, new ItemStack(Items.RAW_IRON), WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<XEIOreVeinDisplay> getRecipeType() {
        return XEIJeiRecipeTypes.ORE_VEIN;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, XEIOreVeinDisplay recipe, IFocusGroup focuses) {
        switch (recipe) {
            case XEIOreVeinDisplay.OreVein oreVein -> {
                int x = 8;
                for (var output : oreVein.outputs()) {
                    if (!output.stack().isEmpty()) {
                        builder.addOutputSlot(x, 24)
                                .setStandardSlotBackground()
                                .addItemStack(output.stack().copy());
                        x += 20;
                    }
                }
            }
            case XEIOreVeinDisplay.BedrockOreVein bedrockOreVein -> {
                // Bedrock ore displays expose material weights, but no concrete item stacks at this layer.
            }
            case XEIOreVeinDisplay.BedrockFluidVein bedrockFluidVein -> {
                Fluid fluid = bedrockFluidVein.fluid();
                builder.addOutputSlot(8, 24)
                        .setStandardSlotBackground()
                        .addFluidStack(fluid, Math.max(1, bedrockFluidVein.maximumYield()));
            }
        }
    }

    @Override
    public void draw(XEIOreVeinDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX,
                     double mouseY) {
        drawHeader(guiGraphics, Component.translatable(recipe.translationKey()));
        drawLine(guiGraphics, Component.literal("ID: " + recipe.id()), 8, 50);
        drawLine(guiGraphics, Component.literal("Weight: " + recipe.weight()), 8, 62);
        drawLine(guiGraphics, Component.literal("Dimensions: " + recipe.dimensions().size()), 8, 74);
    }
}
