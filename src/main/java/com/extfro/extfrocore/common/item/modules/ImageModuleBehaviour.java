package com.extfro.extfrocore.common.item.modules;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.component.IMonitorModuleItem;
import com.extfro.extfrocore.client.renderer.monitor.IMonitorRenderer;
import com.extfro.extfrocore.client.renderer.monitor.MonitorImageRenderer;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.common.machine.multiblock.electric.CentralMonitorMachine;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;

public class ImageModuleBehaviour implements IMonitorModuleItem {

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack) {
        return new MonitorImageRenderer(stack.getOrDefault(GTDataComponents.IMAGE_MODULE_URL, null));
    }

    @Override
    public UIElement createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        UIElement builder = new UIElement().layout(layout -> layout.width(120).height(42));
        TextField textField = new TextField();
        textField.layout(layout -> layout.left(0).top(0).width(100).height(14));
        textField.style(style -> style.background(GuiTextures.DISPLAY));
        textField.textFieldStyle(style -> style.textColor(0x404040).textShadow(false)
                .placeholder(Component.literal("URL")));
        textField.setAnyString();
        textField.setText(stack.getOrDefault(GTDataComponents.IMAGE_MODULE_URL, ""));

        Button saveButton = new Button().noText();
        saveButton.layout(layout -> layout.left(-40).top(22).width(20).height(20));
        saveButton.buttonStyle(style -> style.baseTexture(GuiTextures.BUTTON_CHECK)
                .hoverTexture(GuiTextures.BUTTON_CHECK)
                .pressedTexture(GuiTextures.BUTTON_CHECK));
        saveButton.setOnServerClick(click -> {
            stack.set(GTDataComponents.IMAGE_MODULE_URL, textField.getValue());
        });
        builder.addChild(textField);
        builder.addChild(saveButton);
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
