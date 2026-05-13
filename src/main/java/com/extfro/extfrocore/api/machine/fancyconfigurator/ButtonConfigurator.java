package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.fancy.IFancyConfiguratorButton;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Accessors(chain = true)
public class ButtonConfigurator implements IFancyConfiguratorButton {

    protected IGuiTexture icon;

    protected Consumer<ClickData> onClick;

    @Setter
    protected List<Component> tooltips = Collections.emptyList();

    public ButtonConfigurator(IGuiTexture texture, Consumer<ClickData> onClick) {
        this.icon = texture;
        this.onClick = onClick;
    }

    @Override
    public IGuiTexture getIcon() {
        return icon;
    }

    @Override
    public void onClick(ClickData clickData) {
        onClick.accept(clickData);
    }
}
