package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;

public class ToggleButtonWidget extends Toggle {

    private final IGuiTexture texture;
    private String tooltipText;
    private boolean isMultiLang;

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier isPressedCondition,
                              BooleanConsumer setPressedExecutor) {
        this(xPosition, yPosition, width, height, GuiTextures.VANILLA_BUTTON, isPressedCondition, setPressedExecutor);
    }

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                              BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        super();
        noText();
        layout(layout -> layout.left(xPosition).top(yPosition).width(width).height(height));
        texture = buttonTexture;
        applyTexture(buttonTexture);
        setOn(isPressedCondition.getAsBoolean(), false);
        setOnToggleChanged(value -> setPressedExecutor.accept(value));
        addEventListener(UIEvents.TICK, event -> {
            boolean value = isPressedCondition.getAsBoolean();
            if (getValue() != value) {
                setOn(value, false);
                updateHoverTooltips();
            }
        });
    }

    private void applyTexture(IGuiTexture buttonTexture) {
        IGuiTexture offTexture = buttonTexture;
        IGuiTexture onTexture = buttonTexture;
        if (buttonTexture instanceof ResourceTexture resourceTexture) {
            offTexture = resourceTexture.getSubTexture(0, 0, 1, 0.5);
            onTexture = resourceTexture.getSubTexture(0, 0.5, 1, 0.5);
        }
        IGuiTexture finalOffTexture = offTexture;
        IGuiTexture finalOnTexture = onTexture;
        toggleStyle(style -> style.unmarkTexture(finalOffTexture).markTexture(finalOnTexture));
    }

    public ToggleButtonWidget setShouldUseBaseBackground() {
        if (texture != null) {
            toggleStyle(style -> style
                    .unmarkTexture(new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5),
                            texture))
                    .markTexture(new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                            texture)));
        }
        return this;
    }

    public ToggleButtonWidget setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
        updateHoverTooltips();
        return this;
    }

    public ToggleButtonWidget isMultiLang() {
        isMultiLang = true;
        updateHoverTooltips();
        return this;
    }

    protected void updateHoverTooltips() {
        if (tooltipText != null) {
            var key = tooltipText + (getValue() ? ".enabled" : ".disabled");
            if (!isMultiLang) {
                style(style -> style.tooltips(key));
            } else {
                style(style -> style.tooltips(LangHandler.getMultiLang(key).toArray(Component[]::new)));
            }
        }
    }
}
