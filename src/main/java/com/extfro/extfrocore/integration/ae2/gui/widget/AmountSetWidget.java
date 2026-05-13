package com.extfro.extfrocore.integration.ae2.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.integration.ae2.slot.IConfigurableSlot;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;

import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawStringSized;

public class AmountSetWidget extends UIElement {

    private int index = -1;
    @Getter
    private final TextField amountText;
    private final ConfigWidget parentWidget;
    private final RPCEmitter setAmountRPC = addRPCEvent(RPCEventBuilder.simple(Integer.class, Long.class, this::setAmount));

    public AmountSetWidget(int x, int y, ConfigWidget widget) {
        this.parentWidget = widget;
        layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(80)
                .height(30));
        this.amountText = new TextField();
        this.amountText.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(3)
                .top(12)
                .width(65)
                .height(13));
        this.amountText.setNumbersOnlyLong(0, Integer.MAX_VALUE);
        this.amountText.setTextResponder(this::setNewAmount);
        addChild(this.amountText);
    }

    @OnlyIn(Dist.CLIENT)
    public void setSlotIndexClient(int slotIndex) {
        setSlotIndex(slotIndex);
    }

    public void setSlotIndex(int slotIndex) {
        this.index = slotIndex;
        this.amountText.setText(getAmountStr(), false);
    }

    public String getAmountStr() {
        if (this.index < 0) {
            return "0";
        }
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() != null) {
            return String.valueOf(slot.getConfig().amount());
        }
        return "0";
    }

    public void setNewAmount(String amount) {
        try {
            long newAmount = Long.parseLong(amount);
            if (this.index < 0) {
                return;
            }
            IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
            if (newAmount > 0 && slot.getConfig() != null) {
                setAmountRPC.send(this.index, newAmount);
            }
        } catch (NumberFormatException ignore) {}
    }

    private void setAmount(Integer index, Long amount) {
        if (index == null || amount == null || index < 0) {
            return;
        }
        IConfigurableSlot slot = this.parentWidget.getConfig(index);
        if (amount > 0 && slot.getConfig() != null) {
            slot.setConfig(new GenericStack(slot.getConfig().what(), amount));
            this.parentWidget.slotSync.markAsChanged();
        }
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        super.drawBackgroundAdditional(context);
        int x = Math.round(getPositionX());
        int y = Math.round(getPositionY());
        GuiTextures.BACKGROUND.draw(context, x, y, 80, 30);
        drawStringSized(context.graphics, "Amount", x + 3, y + 3, 0x404040, false, 1f, false);
        GuiTextures.DISPLAY.draw(context, x + 3, y + 11, 65, 14);
    }
}
