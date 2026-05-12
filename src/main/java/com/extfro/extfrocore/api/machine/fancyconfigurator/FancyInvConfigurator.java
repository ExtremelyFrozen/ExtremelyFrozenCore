package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;

import java.util.Collections;
import java.util.List;

public class FancyInvConfigurator implements IFancyConfigurator {

    private final CustomItemStackHandler inventory;
    private final Component title;
    private List<Component> tooltips = Collections.emptyList();

    public FancyInvConfigurator(CustomItemStackHandler inventory, Component title) {
        this.inventory = inventory;
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

    public FancyInvConfigurator setTooltips(List<Component> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.BUTTON_ITEM_OUTPUT;
    }

    @Override
    public UIElement createConfigurator() {
        int rowSize = (int) Math.sqrt(inventory.getSlots());
        int colSize = rowSize;
        if (inventory.getSlots() == 8) {
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
                ItemSlot slot = new ItemSlot();
                slot.bind(inventory, index++);
                slot.layout(layout -> layout.left(4 + x * 18).top(4 + y * 18).width(18).height(18));
                slot.style(style -> style.background(GuiTextures.SLOT));
                slot.xeiRecipeIngredient(IngredientIO.INPUT);
                container.addChild(slot);
            }
        }
        group.addChild(container);
        return group;
    }
}
