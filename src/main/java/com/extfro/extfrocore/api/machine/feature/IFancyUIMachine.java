package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.*;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.extfro.extfrocore.api.machine.fancyconfigurator.MachineModeFancyConfigurator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;

import java.util.ArrayList;
import java.util.List;

public interface IFancyUIMachine extends IUIMachine, IFancyUIProvider {

    @Override
    default ModularUI createUI(Player entityPlayer) {
        return ModularUI.of(UI.of(new FancyMachineUIWidget(this, 176, 166)), entityPlayer);
    }

    /**
     * We should not override this method in general, and use {@link IFancyUIMachine#createUIWidget()} instead,
     */
    @Override
    default UIElement createMainPage(FancyMachineUIWidget widget) {
        return createUIWidget();
    }

    /**
     * Create the core widget of this machine.
     */
    default UIElement createUIWidget() {
        UIElement group = new UIElement().layout(layout -> layout.width(100).height(100));
        group.addChild(new UIElement()
                .layout(layout -> layout.left((100 - 48) / 2f).top(60).width(48).height(16))
                .style(style -> style.background(GuiTextures.SCENE)));
        if (self().isRemote()) {
            TrackedDummyWorld world = new TrackedDummyWorld();
            world.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(self().getBlockState()));
            Scene scene = new Scene()
                    .createScene(world, false, Size.of(100, 100))
                    .useOrtho(true)
                    .setOrthoRange(0.5f)
                    .setScalable(false)
                    .setDraggable(false)
                    .setRenderFacing(false)
                    .setRenderSelect(false);
            scene.layout(layout -> layout.left(0).top(0).width(100).height(100));
            scene.getRenderer().setFov(30);
            scene.setRenderedCore(List.of(BlockPos.ZERO), null);
            group.addChild(scene);
        }
        return group;
    }

    @Override
    default IGuiTexture getTabIcon() {
        return new ItemStackTexture(self().getDefinition().getItem());
    }

    @Override
    default void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);

        if (this instanceof IRecipeLogicMachine rLMachine && rLMachine.getRecipeTypes().length > 1) {
            sideTabs.attachSubTab(new MachineModeFancyConfigurator(rLMachine));
        }
        var directionalConfigurator = CombinedDirectionalFancyConfigurator.of(self(), self());
        if (directionalConfigurator != null)
            sideTabs.attachSubTab(directionalConfigurator);
    }

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        if (this instanceof IControllable controllable) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_POWER,
                    GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, (clickData, pressed) -> controllable.setWorkingEnabled(pressed))
                    .setTooltipsSupplier(pressed -> List.of(
                            Component.translatable(
                                    pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"))));
        }
        if (this instanceof MetaMachine machine) {
            for (var direction : Direction.values()) {
                if (machine.getCoverContainer().hasCover(direction)) {
                    var configurator = machine.getCoverContainer().getCoverAtSide(direction).getConfigurator();
                    if (configurator != null)
                        configuratorPanel.attachConfigurators(configurator);
                }
            }
        }
    }

    @Override
    default void attachTooltips(TooltipsPanel tooltipsPanel) {
        tooltipsPanel.attachTooltips(self());
        self().getTraitHolder().getAllTraits().stream().filter(IFancyTooltip.class::isInstance)
                .map(IFancyTooltip.class::cast)
                .forEach(tooltipsPanel::attachTooltips);
    }

    @Override
    default List<Component> getTabTooltips() {
        var list = new ArrayList<Component>();
        list.add(Component.translatable(self().getDefinition().getDescriptionId()));
        return list;
    }

    @Override
    default Component getTitle() {
        return Component.translatable(self().getDefinition().getDescriptionId());
    }
}
