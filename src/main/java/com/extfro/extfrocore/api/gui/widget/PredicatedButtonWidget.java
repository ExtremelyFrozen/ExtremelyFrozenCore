package com.extfro.extfrocore.api.gui.widget;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PredicatedButtonWidget extends Button {

    private final BooleanSupplier predicate;

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                                  Consumer<ClickData> onPressed, BooleanSupplier predicate, boolean defaultVisibility) {
        this(xPosition, yPosition, width, height, onPressed, predicate);
        buttonStyle(style -> style
                .baseTexture(buttonTexture)
                .hoverTexture(buttonTexture)
                .pressedTexture(buttonTexture));
        setVisible(defaultVisibility);
    }

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                                  Consumer<ClickData> onPressed, BooleanSupplier predicate) {
        this(xPosition, yPosition, width, height, buttonTexture, onPressed, predicate, false);
    }

    public PredicatedButtonWidget(int xPosition, int yPosition, int width, int height, Consumer<ClickData> onPressed,
                                  BooleanSupplier predicate) {
        this.predicate = predicate;
        noText();
        layout(layout -> layout.left(xPosition).top(yPosition).width(width).height(height));
        setOnServerClick(event -> onPressed.accept(new ClickData()));
        addEventListener(UIEvents.TICK, event -> {
            if (this.predicate != null) {
                setVisible(this.predicate.getAsBoolean());
            }
        });
    }
}
