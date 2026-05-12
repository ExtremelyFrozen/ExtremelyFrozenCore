package com.extfro.extfrocore.api.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.utils.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class PatternPreviewSlotWidget extends SlotWidget {

    public PatternPreviewSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                                    boolean canTakeItems, boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    /**
     * Override the draw method for regular slot widget since we do custom offsets when drawing the stack
     */
    @Override
    protected void drawItemStack(GUIContext guiContext, ItemStack itemStack) {
        drawItemStack(guiContext.graphics, itemStack, 0, 0, guiContext.elementColor, null);
    }

    public static void drawItemStack(@NotNull GuiGraphics graphics, ItemStack itemStack, int x, int y, int color,
                                     @Nullable String altTxt) {
        var a = ColorUtils.alpha(color);
        var r = ColorUtils.red(color);
        var g = ColorUtils.green(color);
        var b = ColorUtils.blue(color);
        RenderSystem.setShaderColor(r, g, b, a);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        Minecraft mc = Minecraft.getInstance();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);

        graphics.renderItem(itemStack, x, y);
        graphics.pose().translate(0, 0, 100);

        graphics.pose().pushPose();

        // actual offset bit that's important :3
        int xOffset = 0;
        if (itemStack.getCount() / 100_000 != 0) {
            xOffset = 9;
        } else if (itemStack.getCount() / 10_000 != 0) {
            xOffset = 6;
        } else if (itemStack.getCount() / 1000 != 0) {
            xOffset = 3;
        }

        graphics.renderItemDecorations(mc.font, itemStack, x + xOffset, y, altTxt);
        graphics.pose().popPose();

        graphics.pose().popPose();

        // clear depth buffer,it may cause some rendering issues?
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
    }
}
