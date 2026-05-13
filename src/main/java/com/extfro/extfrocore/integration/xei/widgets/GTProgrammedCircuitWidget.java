package com.extfro.extfrocore.integration.xei.widgets;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;

import java.util.stream.Stream;

public class GTProgrammedCircuitWidget extends UIElement {

    public GTProgrammedCircuitWidget() {
        layout(layout -> layout.width(150).height(80));
        setRecipe();
    }

    public void setRecipe() {
        addChild(GTRecipeElement.fixed(39, 0, 36, 36).style(style -> style.background(GuiTextures.SLOT)));

        ItemStackHandler handler = new CustomItemStackHandler(32);
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 8; i++) {
                int slotIndex = i + j * 8;
                handler.setStackInSlot(slotIndex, IntCircuitBehaviour.stack(1 + slotIndex));
                addChild(GTRecipeElement.itemSlot(3 + 18 * i, 18 * j, GuiTextures.SLOT, "programmed_circuit_" + slotIndex)
                        .bind(handler, slotIndex)
                        .xeiRecipeSlot(slotIndex == 31 ? IngredientIO.OUTPUT : IngredientIO.CATALYST,
                                1, 0, Stream.of(IntCircuitBehaviour.stack(1 + slotIndex))));
            }
        }
    }
}
