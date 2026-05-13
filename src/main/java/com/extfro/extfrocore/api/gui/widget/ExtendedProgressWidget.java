package com.extfro.extfrocore.api.gui.widget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

@Accessors(chain = true)
public class ExtendedProgressWidget extends ProgressBar {

    @Setter
    private Consumer<List<Component>> serverTooltipSupplier;

    public ExtendedProgressWidget() {
        this(() -> 0, 0, 0, 40, 40);
    }

    public ExtendedProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height,
                                  IGuiTexture fullImage) {
        this(progressSupplier, x, y, width, height);
        bar(style -> style.style(s -> s.backgroundTexture(fullImage)));
    }

    public ExtendedProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height) {
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        bindDataSource(SupplierDataSource.of(() -> (float) progressSupplier.getAsDouble()));
    }

    public ExtendedProgressWidget setFillDirection(FillDirection fillDirection) {
        progressBarStyle(style -> style.fillDirection(fillDirection));
        return this;
    }

    public ExtendedProgressWidget setFillDirection(Enum<?> fillDirection) {
        try {
            return setFillDirection(FillDirection.valueOf(fillDirection.name()));
        } catch (IllegalArgumentException ignored) {
            return this;
        }
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        super.drawBackgroundAdditional(guiContext);
        if (serverTooltipSupplier != null && isMouseOverElement(guiContext.mouseX, guiContext.mouseY)) {
            var modularUI = getModularUI();
            if (modularUI == null) {
                return;
            }
            List<Component> tips = new ArrayList<>();
            serverTooltipSupplier.accept(tips);
            if (!tips.isEmpty()) {
                modularUI.setHoverTooltip(tips, ItemStack.EMPTY, null, null);
            }
        }
    }
}
