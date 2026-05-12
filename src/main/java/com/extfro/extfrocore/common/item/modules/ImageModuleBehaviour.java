package com.extfro.extfrocore.common.item.modules;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.component.IMonitorModuleItem;
import com.extfro.extfrocore.client.renderer.monitor.IMonitorRenderer;
import com.extfro.extfrocore.client.renderer.monitor.MonitorImageRenderer;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.machine.multiblock.electric.CentralMonitorMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.extfro.extfrocore.common.network.packets.SCPacketMonitorGroupNBTChange;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import com.lowdragmc.lowdraglib2.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib2.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib2.gui.widget.Widget;
import com.lowdragmc.lowdraglib2.gui.widget.WidgetGroup;

public class ImageModuleBehaviour implements IMonitorModuleItem {

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack) {
        return new MonitorImageRenderer(stack.getOrDefault(GTDataComponents.IMAGE_MODULE_URL, null));
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        TextFieldWidget textField = new TextFieldWidget(0, 0, 100, 10, null, null);
        textField.setCurrentString(stack.getOrDefault(GTDataComponents.IMAGE_MODULE_URL, null));

        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;

            stack.set(GTDataComponents.IMAGE_MODULE_URL, textField.getCurrentString());
            PacketDistributor.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        builder.addWidget(textField);
        builder.addWidget(saveButton);
        return builder;
    }

    @Override
    public String getType() {
        return "image";
    }

    public String getUrl(ItemStack stack) {
        return stack.get(GTDataComponents.IMAGE_MODULE_URL);
    }

    public void setUrl(ItemStack stack, String url) {
        stack.set(GTDataComponents.IMAGE_MODULE_URL, url);
    }
}
