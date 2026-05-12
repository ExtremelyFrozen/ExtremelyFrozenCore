package com.extfro.extfrocore.integration.ae2.gui.widget.slot;

import com.extfro.extfrocore.integration.ae2.gui.widget.ConfigWidget;
import com.extfro.extfrocore.integration.ae2.slot.IConfigurableSlot;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import appeng.api.stacks.GenericStack;

import java.util.ArrayList;

public class AEConfigSlotWidget extends UIElement {

    protected final ConfigWidget parentWidget;
    protected final int index;
    protected boolean select = false;

    public AEConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        this.parentWidget = widget;
        this.index = index;
        layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(18)
                .height(18 * 2));
        addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
            if (slot.getConfig() == null && mouseOverConfig(event.x, event.y)) {
                var tooltips = new ArrayList<Component>();
                tooltips.add(Component.translatable("gtceu.gui.config_slot"));
                if (parentWidget.isAutoPull()) {
                    tooltips.add(Component.translatable("gtceu.gui.config_slot.auto_pull_managed"));
                } else {
                    if (!parentWidget.isStocking()) {
                        tooltips.add(Component.translatable("gtceu.gui.config_slot.set"));
                        tooltips.add(Component.translatable("gtceu.gui.config_slot.scroll"));
                    } else {
                        tooltips.add(Component.translatable("gtceu.gui.config_slot.set_only"));
                    }
                    tooltips.add(Component.translatable("gtceu.gui.config_slot.remove"));
                }
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, null);
            } else {
                GenericStack stack = null;
                if (mouseOverConfig(event.x, event.y)) {
                    stack = slot.getConfig();
                } else if (mouseOverStock(event.x, event.y)) {
                    stack = slot.getStock();
                }
                if (stack != null) {
                    event.hoverTooltips = HoverTooltips.empty().append(
                            Component.literal(stack.what().toString()),
                            Component.literal("x" + stack.amount()));
                }
            }
        });
    }

    public void setSelect(boolean val) {
        this.select = val;
    }

    protected boolean mouseOverConfig(double mouseX, double mouseY) {
        return isMouseOver(getPositionX(), getPositionY(), 18, 18, (float) mouseX, (float) mouseY);
    }

    protected boolean mouseOverStock(double mouseX, double mouseY) {
        return isMouseOver(getPositionX(), getPositionY() + 18, 18, 18, (float) mouseX, (float) mouseY);
    }

    protected boolean isStackValidForSlot(GenericStack stack) {
        return this.parentWidget.isStackValidForSlot(stack);
    }

}
