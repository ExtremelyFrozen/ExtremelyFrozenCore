package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.recipe.*;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.ConfiguratorPanel;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfiguratorButton;
import com.extfro.extfrocore.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.feature.IHasCircuitSlot;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.item.behavior.IntCircuitBehaviour;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ISubscription;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * All simple single machines are implemented here.
 */
public class SimpleTieredMachine extends WorkableTieredMachine
                                 implements IFancyUIMachine, IHasCircuitSlot {

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    @Getter
    @SaveField
    protected final NotifiableItemStackHandler circuitInventory;
    @Nullable
    protected TickableSubscription batterySubs;
    @Nullable
    protected ISubscription energySubs;
    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public SimpleTieredMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        super(info, tier, tankScalingFunction);

        this.autoOutput = attachTrait(new AutoOutputTrait(List.of(exportItems), List.of(exportFluids)));

        this.chargerInventory = new CustomItemStackHandler() {

            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        chargerInventory.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));

        this.circuitInventory = attachTrait(new NotifiableItemStackHandler(1, IO.IN, IO.NONE)
                .shouldDropInventoryInWorld(!ConfigHolder.INSTANCE.machines.ghostCircuit)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateBatterySubscription();
            energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
            chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true)) {
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        } else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////
    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    /// //////////////////////////////////
    // ****** RECIPE LOGIC *******//
    /// //////////////////////////////////

    @Override
    public long getDisplayRecipeVoltage() {
        return EFValues.V[this.tier];
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);

        if (autoOutput.supportsAutoOutputFluids()) {
            configuratorPanel.attachConfigurators(createAutoOutputFluidConfigurator());
        }
        if (autoOutput.supportsAutoOutputItems()) {
            configuratorPanel.attachConfigurators(createAutoOutputItemConfigurator());
        }

        if (isCircuitSlotEnabled()) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    private IFancyConfigurator createAutoOutputFluidConfigurator() {
        return createAutoOutputConfigurator(
                GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON,
                "gtceu.gui.fluid_auto_output",
                this.autoOutput::isAutoOutputFluids,
                (cd, nextState) -> this.autoOutput.setAllowAutoOutputFluids(nextState));
    }

    private IFancyConfigurator createAutoOutputItemConfigurator() {
        return createAutoOutputConfigurator(
                GuiTextures.IO_CONFIG_ITEM_MODES_BUTTON,
                "gtceu.gui.item_auto_output",
                this.autoOutput::isAutoOutputItems,
                (cd, nextState) -> this.autoOutput.setAllowAutoOutputItems(nextState));
    }

    private IFancyConfigurator createAutoOutputConfigurator(ResourceTexture modesButtonTexture,
                                                            String tooltipBaseLangKey,
                                                            BooleanSupplier stateSupplier,
                                                            BiConsumer<ClickData, Boolean> onToggle) {
        var toggle = new IFancyConfiguratorButton.Toggle(
                new GuiTextureGroup(
                        GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5),
                        modesButtonTexture.getSubTexture(0, 1 / 3f, 1, 1 / 3f)),
                new GuiTextureGroup(
                        GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                        modesButtonTexture.getSubTexture(0, 2 / 3f, 1, 1 / 3f)),
                stateSupplier,
                onToggle);

        toggle.setTooltipsSupplier(enabled -> {
            var key = tooltipBaseLangKey + '.' + (enabled ? "enabled" : "disabled");
            return List.of(Component.translatable(key));
        });

        return toggle;
    }

    @Override
    public UIElement createUIWidget() {
        UIElement recipeTemplate = createRecipeTemplate(false);
        UIElement energyGroup = new UIElement().layout(layout -> layout.width(18).height(79));
        energyGroup.addChild(createEnergyBar(this));
        energyGroup.addChild(itemSlot(chargerInventory, 0, 0, 61,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY), true, true,
                Component.translatable("gtceu.gui.charger_slot.tooltip", EFValues.VNF[getTier()],
                        EFValues.VNF[getTier()])));

        int templateWidth = (int) recipeTemplate.getSizeWidth();
        int templateHeight = (int) recipeTemplate.getSizeHeight();
        int groupWidth = Math.max(18 + templateWidth + 4 + 8, 172);
        int groupHeight = Math.max(templateHeight + 8, 87);
        UIElement group = new UIElement().layout(layout -> layout.width(groupWidth).height(groupHeight));

        energyGroup.layout(layout -> layout.left(3).top((groupHeight - 79) / 2f).width(18).height(79));
        recipeTemplate.layout(layout -> layout.left((groupWidth - 18 - 4 - templateWidth) / 2f + 24)
                .top((groupHeight - templateHeight) / 2f));

        group.addChild(energyGroup);
        group.addChild(recipeTemplate);
        return group;
    }

    protected UIElement createRecipeTemplate(boolean generator) {
        var recipeUI = getRecipeType().getRecipeUI();
        UIElement template = recipeUI.createXEIElement(false, false);
        bindRecipeSlots(template, generator);
        var progress = template.selectRegex("^progress$", ProgressBar.class).findFirst();
        UIElement wrapper = new UIElement() {

            @Override
            public void screenTick() {
                progress.ifPresent(progressBar -> progressBar.setProgress(recipeLogic.getProgressPercent()));
                super.screenTick();
            }
        };
        wrapper.layout(layout -> layout.width(template.getSizeWidth()).height(template.getSizeHeight()));
        template.layout(layout -> layout.left(0).top(0));
        wrapper.addChild(template);
        return wrapper;
    }

    protected void bindRecipeSlots(UIElement template, boolean generator) {
        bindItemSlots(template, IO.IN, importItems.storage);
        if (!generator) {
            bindItemSlots(template, IO.OUT, exportItems.storage);
        }
        bindFluidSlots(template, IO.IN, importFluids);
        if (!generator) {
            bindFluidSlots(template, IO.OUT, exportFluids);
        }
    }

    protected void bindItemSlots(UIElement template, IO io, net.neoforged.neoforge.items.IItemHandlerModifiable handler) {
        String regex = "^%s_[0-9]+$".formatted(ItemRecipeCapability.CAP.slotName(io));
        template.selectRegex(regex, ItemSlot.class).forEach(slot -> {
            int index = slotIndex(slot.getId());
            if (index >= 0 && index < handler.getSlots()) {
                slot.bind(handler, index);
            }
        });
    }

    protected void bindFluidSlots(UIElement template, IO io,
                                  com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable handler) {
        String regex = "^%s_[0-9]+$".formatted(FluidRecipeCapability.CAP.slotName(io));
        template.selectRegex(regex, FluidSlot.class).forEach(slot -> {
            int index = slotIndex(slot.getId());
            if (index >= 0 && index < handler.getTanks()) {
                slot.bind(handler, index);
            }
        });
    }

    protected int slotIndex(String id) {
        int idx = id.lastIndexOf('_');
        if (idx < 0 || idx == id.length() - 1) return -1;
        try {
            return Integer.parseInt(id.substring(idx + 1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    // Method provided to override
    protected IGuiTexture getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }
}
