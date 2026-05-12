package com.extfro.extfrocore.integration.ae2.machine.feature.multiblock;

import com.extfro.extfrocore.api.gui.fancy.ConfiguratorPanel;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfiguratorButton;
import com.extfro.extfrocore.api.gui.texture.CroppedTexture;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.network.chat.Component;

import appeng.api.stacks.GenericStack;

import java.util.List;
import java.util.function.Predicate;

public interface IAutoPullPart extends IMultiPart {

    CroppedTexture AUTO_PULL_DISABLED = CroppedTexture.of("extfrocore:textures/gui/widget/button_me_auto_pull.png",
            0, 0, 1, 0.5f);
    CroppedTexture AUTO_PULL_ENABLED = CroppedTexture.of("extfrocore:textures/gui/widget/button_me_auto_pull.png",
            0, 0.5f, 1, 0.5f);

    boolean isAutoPull();

    void setAutoPull(boolean autoPull);

    void setAutoPullTest(Predicate<GenericStack> test);

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                AUTO_PULL_DISABLED,
                AUTO_PULL_ENABLED,
                this::isAutoPull,
                (clickData, pressed) -> setAutoPull(pressed))
                .setTooltipsSupplier(pressed -> List.of(Component.translatable("gtceu.gui.me_bus.auto_pull_button"))));
    }
}
