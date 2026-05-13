package com.extfro.extfrocore.api.gui.widget;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.gui.util.UISoundUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.IntSupplier;

@Setter
@Accessors(chain = true)
public class ColorBlockWidget extends UIElement {

    private IntSupplier colorSupplier;
    @Getter
    private int currentColor;
    private static boolean isShowAlpha = false;

    public ColorBlockWidget(int x, int y, int width, int height) {
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        this.currentColor = 0xFFFFFFFF;
        addEventListener(UIEvents.TICK, event -> {
            if (colorSupplier != null) {
                currentColor = colorSupplier.getAsInt();
            }
        });
        addEventListener(UIEvents.MOUSE_DOWN, event -> {
            UISoundUtils.playButtonClickSound();
            isShowAlpha = !isShowAlpha;
            event.stopPropagation();
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawBackgroundAdditional(GUIContext guiContext) {
        int x = Math.round(getPositionX()) + 1;
        int y = Math.round(getPositionY()) + 1;
        int width = Math.round(getSizeWidth()) - 2;
        int height = Math.round(getSizeHeight()) - 2;

        if (colorSupplier != null) {
            currentColor = colorSupplier.getAsInt();
        }
        int opaqueColor = isShowAlpha ? currentColor : currentColor | 0xFF000000;
        guiContext.graphics.fill(x, y, x + width, y + height, opaqueColor);
        DrawerHelper.drawBorder(guiContext.graphics, x, y, width, height, 0xFF000000, 1);
    }
}
