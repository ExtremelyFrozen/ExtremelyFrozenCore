package com.extfro.extfrocore.integration.jei.category;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Nullable;

abstract class AbstractXEIJeiCategory<T> implements IRecipeCategory<T> {

    private static final int TEXT_COLOR = 0xFF404040;
    private final Component title;
    @Nullable
    private final IDrawable icon;
    private final int width;
    private final int height;

    protected AbstractXEIJeiCategory(IGuiHelper guiHelper, Component title, ItemStack icon, int width, int height) {
        this.title = title;
        this.icon = icon.isEmpty() ? null : guiHelper.createDrawableItemStack(icon);
        this.width = width;
        this.height = height;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    protected void drawLine(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, TEXT_COLOR, false);
    }

    protected void drawHeader(GuiGraphics graphics, Component text) {
        drawLine(graphics, text, 4, 4);
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawHeader(guiGraphics, getTitle());
    }
}
