package com.extfro.extfrocore.api.machine.feature.multiblock;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.UITemplate;
import com.extfro.extfrocore.api.machine.feature.IUIMachine;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;

import java.util.List;

public interface IDisplayUIMachine extends IUIMachine {

    default void addDisplayText(List<Component> textList) {
        for (var part : self().getParts()) {
            part.addMultiText(textList);
        }
    }

    default void handleDisplayClick(String componentData, ClickData clickData) {}

    default void addDisplayControls(UIElement display) {}

    default IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY;
    }

    @Override
    default ModularUI createUI(Player entityPlayer) {
        UIElement screen = MachineUIHelper.group(7, 4, 162, 121)
                .style(style -> style.background(getScreenTexture()));
        screen.addChild(MachineUIHelper.label(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addChild(MachineUIHelper.componentPanel(4, 17, 150, 10, this::addDisplayText));
        addDisplayControls(screen);
        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 134, true));
    }

    @Override
    default MultiblockControllerMachine self() {
        return (MultiblockControllerMachine) this;
    }
}
