package com.extfro.extfrocore.api.machine.fancyconfigurator;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.FancyMachineUIWidget;
import com.extfro.extfrocore.api.gui.fancy.IFancyUIProvider;
import com.extfro.extfrocore.api.gui.widget.directional.CombinedDirectionalConfigurator;
import com.extfro.extfrocore.api.gui.widget.directional.IDirectionalConfigHandler;
import com.extfro.extfrocore.api.gui.widget.directional.handlers.AutoOutputFluidConfigHandler;
import com.extfro.extfrocore.api.gui.widget.directional.handlers.AutoOutputItemConfigHandler;
import com.extfro.extfrocore.api.gui.widget.directional.handlers.CoverableConfigHandler;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class CombinedDirectionalFancyConfigurator implements IFancyUIProvider {

    private final List<Supplier<IDirectionalConfigHandler>> configs;
    private final MetaMachine machine;

    public CombinedDirectionalFancyConfigurator(List<Supplier<IDirectionalConfigHandler>> configs,
                                                MetaMachine machine) {
        this.configs = configs;
        this.machine = machine;
    }

    @Override
    public UIElement createMainPage(FancyMachineUIWidget widget) {
        int parentWidth = Math.max(176, (int) widget.getSizeWidth());
        int parentHeight = Math.max(166, (int) widget.getSizeHeight());
        return new CombinedDirectionalConfigurator(
                widget, configs.stream().map(Supplier::get).toArray(IDirectionalConfigHandler[]::new), machine,
                parentWidth - 8,
                parentHeight - 82);
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.TOOL_COVER_SETTINGS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.directional_setting.title");
    }

    @Override
    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtceu.gui.directional_setting.tab_tooltip"));
        return tooltip;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final List<Function<MetaMachine, Supplier<IDirectionalConfigHandler>>> CONFIG_HANDLERS = new ArrayList<>();

    static {
        // Left side:
        CONFIG_HANDLERS.add(
                machine -> {
                    var trait = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);
                    return trait != null && trait.supportsAutoOutputItems() ?
                            () -> new AutoOutputItemConfigHandler(trait) : null;
                });
        CONFIG_HANDLERS.add(
                machine -> {
                    var trait = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);
                    return trait != null && trait.supportsAutoOutputFluids() ?
                            () -> new AutoOutputFluidConfigHandler(trait) : null;
                });

        // Right side:
        CONFIG_HANDLERS.add(machine -> () -> new CoverableConfigHandler(machine.getCoverContainer()));
    }

    /**
     * To be used by addons for registering their own directional configurations
     */
    public static void registerConfigHandler(Function<MetaMachine, Supplier<IDirectionalConfigHandler>> factory) {
        CONFIG_HANDLERS.add(factory);
    }

    @Nullable
    public static CombinedDirectionalFancyConfigurator of(MetaMachine container, MetaMachine machine) {
        var configs = CONFIG_HANDLERS.stream()
                .map(handler -> handler.apply(container))
                .filter(Objects::nonNull)
                .toList();

        return configs.isEmpty() ? null : new CombinedDirectionalFancyConfigurator(configs, machine);
    }
}
