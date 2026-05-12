package com.extfro.extfrocore.integration.ae2.gui.widget.list;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.client.TooltipsHandler;
import com.extfro.extfrocore.integration.ae2.utils.AEUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;

import java.util.ArrayList;

/**
 * Display a certain {@link FluidStack} element.
 */
public class AEFluidDisplayWidget extends AEListGridWidget.DisplayElement {

    public AEFluidDisplayWidget(AEListGridWidget gridWidget, int index) {
        super(gridWidget, index);
        addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            GenericStack fluid = this.gridWidget.getAt(this.index);
            if (fluid != null) {
                FluidStack fluidStack = AEUtil.toFluidStack(fluid);
                var tooltips = new ArrayList<Component>();
                tooltips.add(fluidStack.getHoverName());
                tooltips.add(Component.literal(String.format("%,d mB", fluid.amount())));
                TooltipsHandler.appendFluidTooltips(fluidStack, tooltips::add,
                        TooltipFlag.NORMAL, Item.TooltipContext.of(Minecraft.getInstance().level));
                event.hoverTooltips = new HoverTooltips(tooltips, null, null, null);
            }
        });
    }

    @Override
    protected void drawSlot(GUIContext context, int x, int y) {
        GuiTextures.FLUID_SLOT.draw(context.graphics, context.mouseX, context.mouseY, x, y, 18, 18);
    }

    @Override
    protected void drawStack(GUIContext context, GenericStack stack, int x, int y) {
        DrawerHelper.drawFluidForGui(context.graphics, AEUtil.toFluidStack(stack), x, y, 16, 16);
    }
}
