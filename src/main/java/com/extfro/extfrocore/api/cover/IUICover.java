package com.extfro.extfrocore.api.cover;

import com.extfro.extfrocore.api.gui.GuiTextures;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;

public interface IUICover {

    int COVER_UI_WIDTH = 176;
    int PLAYER_INVENTORY_HEIGHT = 82;

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    default boolean isInvalid() {
        return self().coverHolder.isRemoved() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    default ModularUI createUI(Player entityPlayer) {
        int height = getUIHeight();
        UIElement root = new UIElement()
                .layout(layout -> layout.width(COVER_UI_WIDTH).height(height + PLAYER_INVENTORY_HEIGHT))
                .style(style -> style.background(GuiTextures.BACKGROUND));

        UIElement coverElement = createUIElement();
        coverElement.layout(layout -> layout.left((COVER_UI_WIDTH - getUIWidth()) / 2f).top(0));
        root.addChild(coverElement);

        InventorySlots inventory = new InventorySlots();
        inventory.layout(layout -> layout.left(7).top(height).width(162).height(76));
        inventory.apply(slot -> slot.style(style -> style.background(GuiTextures.SLOT)));
        root.addChild(inventory);

        root.addEventListener("removed", event -> onUIClosed());
        return ModularUI.of(UI.of(root), entityPlayer);
    }

    default void onUIClosed() {}

    default int getUIWidth() {
        return 178;
    }

    default int getUIHeight() {
        return 85;
    }

    UIElement createUIElement();

    default void markAsDirty() {}
}
