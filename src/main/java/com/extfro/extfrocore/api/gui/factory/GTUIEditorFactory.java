package com.extfro.extfrocore.api.gui.factory;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.editor.GTUIEditor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.factory.IContainerUIHolder;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;

public class GTUIEditorFactory implements IContainerUIHolder {

    public static final GTUIEditorFactory INSTANCE = new GTUIEditorFactory();
    public static final ResourceLocation UI_ID = LDLib2.id("gt_ui_editor");

    private GTUIEditorFactory() {
        PlayerUIMenuType.register(UI_ID, player -> this::createUI);
    }

    public boolean openUI(Player player) {
        return PlayerUIMenuType.openUI(player, UI_ID);
    }

    public boolean openUI(GTUIEditorFactory ignored, Player player) {
        return openUI(player);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return ModularUI.of(UI.of(new GTUIEditor()), entityPlayer);
    }

    @Override
    public boolean isStillValid(Player player) {
        return true;
    }

    public boolean isRemote() {
        return ExtForCore.isClientThread();
    }
}
