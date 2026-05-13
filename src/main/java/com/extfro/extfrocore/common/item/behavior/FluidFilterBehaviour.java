package com.extfro.extfrocore.common.item.behavior;

import com.extfro.extfrocore.api.cover.filter.FluidFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.component.IItemUIFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;

import java.util.function.Function;

public record FluidFilterBehaviour(Function<ItemStack, FluidFilter> filterCreator) implements IItemUIFactory {

    @Override
    public void onAttached(Item item) {
        IItemUIFactory.super.onAttached(item);
        FluidFilter.FILTERS.put(item, filterCreator);
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        var held = holder.itemStack;
        UIElement root = new UIElement()
                .layout(layout -> layout.width(176).height(157))
                .style(style -> style.background(GuiTextures.BACKGROUND));
        Label label = new Label();
        label.setValue(Component.translatable(held.getDescriptionId()));
        label.layout(layout -> layout.left(5).top(5).width(120).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        root.addChild(label);
        root.addChild(FluidFilter.loadFilter(held).openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15));
        InventorySlots inventory = new InventorySlots();
        inventory.layout(layout -> layout.left(7).top(75).width(162).height(76));
        inventory.apply(slot -> slot.style(style -> style.background(GuiTextures.SLOT)));
        root.addChild(inventory);
        return ModularUI.of(UI.of(root), holder.player);
    }
}
