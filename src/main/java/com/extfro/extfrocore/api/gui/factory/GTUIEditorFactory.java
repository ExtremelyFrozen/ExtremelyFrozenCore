package com.extfro.extfrocore.api.gui.factory;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.editor.GTUIEditor;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.LDLib;
import com.lowdragmc.lowdraglib2.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib2.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class GTUIEditorFactory extends UIFactory<GTUIEditorFactory> implements IUIHolder {

    public static final GTUIEditorFactory INSTANCE = new GTUIEditorFactory();

    private GTUIEditorFactory() {
        super(LDLib.location("gt_ui_editor"));
    }

    @Override
    protected ModularUI createUITemplate(GTUIEditorFactory holder, Player entityPlayer) {
        return createUI(entityPlayer);
    }

    @Override
    protected GTUIEditorFactory readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        return this;
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, GTUIEditorFactory holder) {}

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(this, entityPlayer).widget(new GTUIEditor());
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return ExtForCore.isClientThread();
    }

    @Override
    public void markAsDirty() {}
}
