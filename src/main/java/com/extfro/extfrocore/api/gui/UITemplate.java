package com.extfro.extfrocore.api.gui;

import com.extfro.extfrocore.api.gui.widget.SlotWidget;

import net.minecraft.world.entity.player.Inventory;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

public class UITemplate {

    public static UIElement bindPlayerInventory(Inventory inventoryPlayer, IGuiTexture imageLocation, int x, int y,
                                                boolean addHotbar) {
        UIElement group = new UIElement()
                .layout(layout -> layout.left(x).top(y).width(162).height(54 + (addHotbar ? 22 : 0)));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                group.addChild(new SlotWidget(inventoryPlayer, col + (row + 1) * 9, col * 18, row * 18)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, false));
            }
        }
        if (addHotbar) {
            for (int slot = 0; slot < 9; slot++) {
                group.addChild(new SlotWidget(inventoryPlayer, slot, slot * 18, 58)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, true));
            }
        }
        return group;
    }
}
