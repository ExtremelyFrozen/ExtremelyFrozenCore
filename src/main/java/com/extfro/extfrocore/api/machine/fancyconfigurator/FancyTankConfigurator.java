package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.transfer.fluid.CustomFluidTank;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;

import java.util.Collections;
import java.util.List;

public class FancyTankConfigurator implements IFancyConfigurator {

    private final CustomFluidTank[] tanks;
    private final Component title;
    private List<Component> tooltips = Collections.emptyList();

    public FancyTankConfigurator(CustomFluidTank[] tanks, Component title) {
        this.tanks = tanks;
        this.title = title;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public List<Component> getTooltips() {
        return tooltips;
    }

    public FancyTankConfigurator setTooltips(List<Component> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.BUTTON_FLUID_OUTPUT;
    }

    @Override
    public UIElement createConfigurator() {
        int rowSize = (int) Math.sqrt(tanks.length);
        int colSize = rowSize;
        if (tanks.length == 8) {
            rowSize = 4;
            colSize = 2;
        }

        UIElement group = new UIElement()
                .layout(layout -> layout.width(18 * rowSize + 16).height(18 * colSize + 16));
        UIElement container = new UIElement()
                .layout(layout -> layout.left(4).top(4).width(18 * rowSize + 8).height(18 * colSize + 8))
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                FluidSlot slot = new FluidSlot();
                slot.bind(tanks[index++], 0);
                slot.layout(layout -> layout.left(4 + x * 18).top(4 + y * 18).width(18).height(18));
                slot.style(style -> style.background(GuiTextures.FLUID_SLOT));
                container.addChild(slot);
            }
        }

        group.addChild(container);
        return group;
    }
}
