package com.extfro.extfrocore.api.machine.feature.multiblock;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.ConfiguratorPanel;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfiguratorButton;
import com.extfro.extfrocore.api.gui.texture.CroppedTexture;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public interface IDistinctPart extends IMultiPart {

    boolean isDistinct();

    void setDistinct(boolean isDistinct);

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        superAttachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                CroppedTexture.of(GuiTextures.BUTTON_DISTINCT_BUSES, 0, 0.5f, 1, 0.5f),
                CroppedTexture.of(GuiTextures.BUTTON_DISTINCT_BUSES, 0, 0, 1, 0.5f),
                this::isDistinct, (clickData, pressed) -> setDistinct(pressed))
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("gtceu.multiblock.universal.distinct")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                                .append(Component.translatable(pressed ? "gtceu.multiblock.universal.distinct.yes" :
                                        "gtceu.multiblock.universal.distinct.no")))));
    }

    default void superAttachConfigurators(ConfiguratorPanel configuratorPanel) {
        IMultiPart.super.attachConfigurators(configuratorPanel);
    }
}
