package com.extfro.extfrocore.common.cover.data;

import com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import lombok.Getter;

public enum TransferMode implements EnumSelectorWidget.SelectableEnum {

    TRANSFER_ANY("cover.robotic_arm.transfer_mode.transfer_any", "transfer_any", 1),
    TRANSFER_EXACT("cover.robotic_arm.transfer_mode.transfer_exact", "transfer_exact", 1024),
    KEEP_EXACT("cover.robotic_arm.transfer_mode.keep_exact", "keep_exact", 1024);

    @Getter
    public final String tooltip;
    @Getter
    public final IGuiTexture icon;
    public final int maxStackSize;

    TransferMode(String tooltip, String textureName, int maxStackSize) {
        this.tooltip = tooltip;
        this.maxStackSize = maxStackSize;
        this.icon = SpriteTexture.of("extfrocore:textures/gui/icon/transfer_mode/" + textureName + ".png");
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public IGuiTexture getIcon() {
        return icon;
    }
}
