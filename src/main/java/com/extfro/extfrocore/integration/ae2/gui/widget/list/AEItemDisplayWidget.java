package com.extfro.extfrocore.integration.ae2.gui.widget.list;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.integration.ae2.utils.AEUtil;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;

import static com.lowdragmc.lowdraglib2.gui.util.DrawerHelper.drawItemStack;

/**
 * Display a certain {@link GenericStack} item element.
 */
public class AEItemDisplayWidget extends AEListGridWidget.DisplayElement {

    public AEItemDisplayWidget(AEListGridWidget gridWidget, int index) {
        super(gridWidget, index);
        addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            GenericStack item = this.gridWidget.getAt(this.index);
            if (item != null) {
                event.hoverTooltips = HoverTooltips.empty().stack(AEUtil.toItemStack(item));
            }
        });
    }

    @Override
    protected void drawSlot(GUIContext context, int x, int y) {
        GuiTextures.SLOT.draw(context.graphics, context.mouseX, context.mouseY, x, y, 18, 18);
    }

    @Override
    protected void drawStack(GUIContext context, GenericStack stack, int x, int y) {
        ItemStack itemStack = AEUtil.toItemStack(stack);
        drawItemStack(context.graphics, itemStack, x, y, -1, null);
    }
}
