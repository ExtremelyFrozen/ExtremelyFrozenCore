package com.extfro.extfrocore.client.renderer.item.decorator;

import com.extfro.extfrocore.api.item.IGTTool;
import com.extfro.extfrocore.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNull;

public class GTToolBarRenderer implements IItemDecorator {

    public static final GTToolBarRenderer INSTANCE = new GTToolBarRenderer();

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, ItemStack stack, int x, int y) {
        if (stack.getItem() instanceof IGTTool gtTool) {
            return ToolChargeBarRenderer.renderBarsTool(guiGraphics, gtTool, stack, x, y);
        }
        return false;
    }
}
