package com.extfro.extfrocore.api.gui.fancy;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipsPanel extends UIElement {

    protected final List<IFancyTooltip> tooltips = new ArrayList<>();

    public TooltipsPanel() {
        layout(layout -> layout.left(202).top(2).width(20).height(0));
    }

    public List<IFancyTooltip> getTooltips() {
        return tooltips;
    }

    public void clear() {
        tooltips.clear();
        clearAllChildren();
        layout(layout -> layout.height(0));
    }

    public void attachTooltips(IFancyTooltip... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
        rebuild();
    }

    private void rebuild() {
        clearAllChildren();
        int y = 0;
        for (IFancyTooltip tooltip : this.tooltips) {
            if (!tooltip.showFancyTooltip()) continue;
            Button button = new Button().noText();
            button.layout(layout -> layout.left(0).top(y).width(20).height(20));
            button.buttonStyle(style -> style
                    .baseTexture(tooltip.getFancyTooltipIcon())
                    .hoverTexture(tooltip.getFancyTooltipIcon())
                    .pressedTexture(tooltip.getFancyTooltipIcon()));
            button.style(style -> style.tooltips(tooltip.getFancyTooltip().toArray(net.minecraft.network.chat.Component[]::new)));
            addChild(button);
            y += 22;
        }
        int height = Math.max(0, y);
        layout(layout -> layout.height(height));
    }
}
