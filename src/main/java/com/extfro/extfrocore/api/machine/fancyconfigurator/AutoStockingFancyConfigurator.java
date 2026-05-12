package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.integration.ae2.machine.MEStockingBusPartMachine;
import com.extfro.extfrocore.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;

public class AutoStockingFancyConfigurator implements IFancyConfigurator {

    private final IMEStockingPart machine;

    public AutoStockingFancyConfigurator(IMEStockingPart machine) {
        this.machine = machine;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.adv_stocking_config.title");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(GTItems.TOOL_DATA_STICK.asStack());
    }

    @Override
    public UIElement createConfigurator() {
        UIElement group = new UIElement().layout(layout -> layout.width(90).height(70));

        String suffix = machine instanceof MEStockingBusPartMachine ? "min_item_count" : "min_fluid_count";

        group.addChild(label(4, 2, "gtceu.gui.title.adv_stocking_config." + suffix));
        group.addChild(intField(4, 12, 81, 14, machine.getMinStackSize(), 1, Integer.MAX_VALUE,
                machine::setMinStackSize,
                Component.translatable("gtceu.gui.adv_stocking_config." + suffix)));
        group.addChild(label(4, 36, "gtceu.gui.title.adv_stocking_config.ticks_per_cycle"));
        group.addChild(intField(4, 46, 81, 14, machine.getTicksPerCycle(),
                ConfigHolder.INSTANCE.compat.ae2.updateIntervals, Integer.MAX_VALUE,
                machine::setTicksPerCycle,
                Component.translatable("gtceu.gui.adv_stocking_config.ticks_per_cycle")));

        return group;
    }

    private static Label label(int x, int y, String key) {
        Label label = new Label();
        label.setValue(Component.translatable(key));
        label.layout(layout -> layout.left(x).top(y).width(82).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private static TextField intField(int x, int y, int width, int height, int value, int min, int max,
                                      java.util.function.IntConsumer setter, Component tooltip) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(height));
        field.style(style -> style.background(GuiTextures.NUMBER_BACKGROUND).tooltips(tooltip));
        field.setNumbersOnlyInt(min, max);
        field.setText(Integer.toString(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Math.clamp(Integer.parseInt(text), min, max));
            }
        });
        return field;
    }
}
