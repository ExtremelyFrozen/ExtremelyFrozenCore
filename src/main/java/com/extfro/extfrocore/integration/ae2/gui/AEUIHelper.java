package com.extfro.extfrocore.integration.ae2.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.TaffyPosition;

import java.util.function.Supplier;

import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawGradientRect;

public final class AEUIHelper {

    private AEUIHelper() {}

    public static UIElement group(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(width)
                .height(height));
    }

    public static Label label(int x, int y, String translationKey) {
        return label(x, y, 150, 10, () -> Component.translatable(translationKey));
    }

    public static Label label(int x, int y, Supplier<Component> supplier) {
        return label(x, y, 150, 10, supplier);
    }

    public static Label label(int x, int y, int width, int height, Supplier<Component> supplier) {
        Label label = new Label();
        label.setValue(supplier.get());
        label.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(width)
                .height(height));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        label.addEventListener(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.TICK,
                event -> label.setValue(supplier.get()));
        return label;
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawSelectionOverlay(GuiGraphics graphics, int x, int y, int width, int height) {
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.colorMask(true, true, true, false);
        drawGradientRect(graphics, x, y, width, height, -2130706433, -2130706433);
        com.mojang.blaze3d.systems.RenderSystem.colorMask(true, true, true, true);
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    }
}
