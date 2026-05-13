package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ShaderTexture;
import lombok.Getter;

/**
 * The capability can be input or output or both
 */
public enum IO implements EnumSelectorWidget.SelectableEnum {

    IN("extfrocore.io.import", "import"),
    OUT("extfrocore.io.export", "export"),
    BOTH("extfrocore.io.both", "both"),
    NONE("extfrocore.io.none", "none");

    @Getter
    public final String tooltip;
    @Getter
    public final IGuiTexture icon;

    IO(String tooltip, String textureName) {
        this.tooltip = tooltip;
        this.icon = new ShaderTexture(ExtForCore.id("textures/gui/icon/io_mode/" + textureName + ".png"));
    }

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }
}
