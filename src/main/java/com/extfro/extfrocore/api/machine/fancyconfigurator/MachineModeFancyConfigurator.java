package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;
import com.extfro.extfrocore.api.gui.fancy.IFancyUIProvider;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.common.data.GTItems;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;

import java.util.ArrayList;
import java.util.List;

public class MachineModeFancyConfigurator implements IFancyUIProvider {

    protected final IRecipeLogicMachine machine;

    public MachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.machinemode.title");
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.ROBOT_ARM_LV.get());
    }

    @Override
    public UIElement createMainPage(FancyMachineUIWidget widget) {
        UIElement group = new UIElement()
                .layout(layout -> layout.width(140).height(20 * machine.getRecipeTypes().length + 4))
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        for (int i = 0; i < machine.getRecipeTypes().length; i++) {
            int finalI = i;
            Button button = new Button().noText();
            button.layout(layout -> layout.left(2).top(2 + finalI * 20).width(136).height(20));
            button.buttonStyle(style -> style
                    .baseTexture(modeTexture(finalI))
                    .hoverTexture(modeTexture(finalI))
                    .pressedTexture(modeTexture(finalI)));
            button.setOnServerClick(event -> setActiveRecipeTypeAndUpdateTickSubs(finalI));
            group.addChild(button);
        }
        return group;
    }

    private IGuiTexture modeTexture(int index) {
        return new GuiTextureGroup(GuiTextures.BUTTON.copy()
                .setColor(machine.getActiveRecipeType() == index ? 0xff00ffff : -1),
                new TextTexture(machine.getRecipeTypes()[index].getTranslationKey()).setWidth(136)
                        .setType(TextTexture.TextType.ROLL));
    }

    @Override
    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtceu.gui.machinemode.tab_tooltip"));
        return tooltip;
    }

    private void setActiveRecipeTypeAndUpdateTickSubs(int activeRecipeType) {
        boolean needUpdateTickSubs = !machine.keepSubscribing() && activeRecipeType != machine.getActiveRecipeType();
        machine.setActiveRecipeType(activeRecipeType);
        if (needUpdateTickSubs) {
            machine.getRecipeLogic().updateTickSubscription();
        }
    }
}
