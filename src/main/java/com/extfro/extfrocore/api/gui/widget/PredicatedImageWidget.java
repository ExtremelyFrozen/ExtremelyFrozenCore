package com.extfro.extfrocore.api.gui.widget;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.BooleanSupplier;

@Accessors(chain = true)
public class PredicatedImageWidget extends UIElement {

    @Setter
    private BooleanSupplier predicate;

    public PredicatedImageWidget(int xPosition, int yPosition, int width, int height, IGuiTexture area) {
        layout(layout -> layout.left(xPosition).top(yPosition).width(width).height(height));
        style(style -> style.background(area));
        addEventListener(UIEvents.TICK, event -> setVisible(predicate == null || predicate.getAsBoolean()));
    }
}
