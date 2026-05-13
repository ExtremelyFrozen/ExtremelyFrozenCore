package com.extfro.extfrocore.integration.ae2.gui.widget.slot;

import com.extfro.extfrocore.api.gui.widget.SlotWidget;

import net.minecraft.world.Container;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;

public class AEPatternViewSlotWidget extends SlotWidget {

    protected IGuiTexture occupiedTexture;

    public AEPatternViewSlotWidget() {}

    public AEPatternViewSlotWidget(Container inventory,
                                   int slotIndex,
                                   int xPosition,
                                   int yPosition,
                                   boolean canTakeItems,
                                   boolean canPutItems) {
        super(inventory, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public AEPatternViewSlotWidget(IItemHandlerModifiable itemHandler,
                                   int slotIndex,
                                   int xPosition,
                                   int yPosition,
                                   boolean canTakeItems,
                                   boolean canPutItems) {
        super(itemHandler, slotIndex, xPosition, yPosition, canTakeItems, canPutItems);
    }

    public AEPatternViewSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    public AEPatternViewSlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition);
    }

    public AEPatternViewSlotWidget setOccupiedTexture(IGuiTexture... occupiedTexture) {
        this.occupiedTexture = occupiedTexture.length > 1 ? new GuiTextureGroup(occupiedTexture) : occupiedTexture[0];
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public void drawBackgroundAdditional(GUIContext guiContext) {
        super.drawBackgroundAdditional(guiContext);
        if (!getValue().isEmpty() && occupiedTexture != null) {
            occupiedTexture.draw(guiContext, 0, 0, getSizeWidth(), getSizeHeight());
        }
    }
}
