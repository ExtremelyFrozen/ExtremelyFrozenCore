package com.extfro.extfrocore.api.item.component;

import com.extfro.extfrocore.api.placeholder.PlaceholderContext;
import com.extfro.extfrocore.client.renderer.monitor.IMonitorRenderer;
import com.extfro.extfrocore.common.machine.multiblock.electric.CentralMonitorMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

public interface IMonitorModuleItem extends IItemComponent {

    default void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {}

    default void tickInPlaceholder(ItemStack stack, PlaceholderContext context) {}

    IMonitorRenderer getRenderer(ItemStack stack);

    UIElement createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group);

    default String getType() {
        return "unknown";
    }
}
