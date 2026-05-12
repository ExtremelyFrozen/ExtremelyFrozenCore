package com.extfro.extfrocore.api.machine.multiblock;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IEnergyContainer;
import com.extfro.extfrocore.api.capability.recipe.EURecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeHandler;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.*;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.feature.IOverclockMachine;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;
import com.extfro.extfrocore.api.machine.feature.IVoidable;
import com.extfro.extfrocore.api.machine.feature.multiblock.IDisplayUIMachine;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.misc.EnergyContainerList;
import com.extfro.extfrocore.api.recipe.modifier.RecipeModifierList;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.common.data.GTRecipeModifiers;
import com.extfro.extfrocore.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class WorkableElectricMultiblockMachine extends WorkableMultiblockMachine implements IFancyUIMachine,
                                               IDisplayUIMachine, ITieredMachine, IOverclockMachine {

    // runtime
    protected EnergyContainerList energyContainer;
    @Getter
    protected int tier;
    @SaveField
    @Getter
    protected boolean batchEnabled;

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info,
                                             Function<WorkableMultiblockMachine, RecipeLogic> recipeLogicSupplier) {
        super(info, recipeLogicSupplier);
    }

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info, RecipeLogic recipeLogic) {
        super(info, recipeLogic);
    }

    public WorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public WorkableElectricMultiblockMachine self() {
        return this;
    }

    //////////////////////////////////////
    // *** Multiblock Lifecycle ***//
    //////////////////////////////////////
    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = null;
        this.tier = 0;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.energyContainer = getEnergyContainer();
        this.tier = GTUtil.getFloorTierByVoltage(getMaxVoltage());
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.energyContainer = null;
        this.tier = 0;
    }

    @Override
    public void setBatchEnabled(boolean batch) {
        this.batchEnabled = batch;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        int numParallels;
        int subtickParallels;
        int batchParallels;
        int totalRuns;
        boolean exact = false;
        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            subtickParallels = recipeLogic.getLastRecipe().subtickParallels;
            batchParallels = recipeLogic.getLastRecipe().batchParallels;
            totalRuns = recipeLogic.getLastRecipe().getTotalRuns();
            exact = true;
        } else {
            numParallels = getParallelHatch()
                    .map(ParallelHatchPartMachine::getCurrentParallel)
                    .orElse(0);
            subtickParallels = 0;
            batchParallels = 0;
            totalRuns = 0;
        }

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1)
                .addTotalRunsLine(totalRuns)
                .addParallelsLine(numParallels, exact)
                .addSubtickParallelsLine(subtickParallels)
                .addBatchModeLine(isBatchEnabled(), batchParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic)
                .addRecipeFailReasonLine(recipeLogic)
                .addOutputLines(recipeLogic.getLastRecipe());
        getDefinition().getAdditionalDisplay().accept(this, textList);
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    @Override
    public UIElement createUIWidget() {
        var group = new UIElement().layout(layout -> layout.width(190).height(125))
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        var screen = new ScrollerView();
        screen.layout(layout -> layout.left(4).top(4).width(182).height(117));
        screen.style(style -> style.background(getScreenTexture()));

        var title = label(4, 5, 174, 10, Component.translatable(self().getBlockState().getBlock().getDescriptionId()));
        screen.addScrollViewChild(title);

        List<Component> displayText = new ArrayList<>();
        addDisplayText(displayText);
        int y = 17;
        for (Component component : displayText) {
            Label line = label(4, y, 174, 10, component);
            line.style(style -> style.tooltips(component));
            screen.addScrollViewChild(line);
            y += 10;
        }
        group.addChild(screen);
        return group;
    }

    protected Label label(int x, int y, int width, int height, Component component) {
        Label label = new Label();
        label.setValue(component);
        label.layout(layout -> layout.left(x).top(y).width(width).height(height));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(Objects::nonNull).map(IFancyUIProvider.class::cast).toList();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IVoidable.attachConfigurators(configuratorPanel, this);
        if (getDefinition().getRecipeModifier() instanceof RecipeModifierList list && Arrays.stream(list.getModifiers())
                .anyMatch(modifier -> modifier == GTRecipeModifiers.BATCH_MODE)) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_BATCH.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_BATCH.getSubTexture(0, 0.5, 1, 0.5),
                    this::isBatchEnabled,
                    (cd, p) -> setBatchEnabled(p))
                    .setTooltipsSupplier(
                            p -> List.of(
                                    Component.translatable("gtceu.machine.batch_" + (p ? "enabled" : "disabled")))));
        }

        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////
    @Override
    public int getOverclockTier() {
        return getTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getTier();
    }

    @Override
    public int getMinOverclockTier() {
        return getTier();
    }

    @Override
    public void setOverclockTier(int tier) {}

    @Override
    public long getOverclockVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        long voltage;
        long amperage;
        if (energyContainer.getInputVoltage() > energyContainer.getOutputVoltage()) {
            voltage = energyContainer.getInputVoltage();
            amperage = energyContainer.getInputAmperage();
        } else {
            voltage = energyContainer.getOutputVoltage();
            amperage = energyContainer.getOutputAmperage();
        }

        if (amperage == 1) {
            // amperage is 1 when the energy is not exactly on a tier
            // the voltage for recipe search is always on tier, so take the closest lower tier
            return EFValues.VEX[GTUtil.getFloorTierByVoltage(voltage)];
        } else {
            // amperage != 1 means the voltage is exactly on a tier
            // ignore amperage, since only the voltage is relevant for recipe search
            // amps are never > 3 in an EnergyContainerList
            return voltage;
        }
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    public EnergyContainerList getEnergyContainer() {
        List<IEnergyContainer> containers = new ArrayList<>();
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers.isEmpty()) handlers = getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IEnergyContainer container) {
                containers.add(container);
            }
        }
        return new EnergyContainerList(containers);
    }

    @Override
    public long getMaxVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        if (this.isGenerator()) {
            // Generators
            long voltage = energyContainer.getOutputVoltage();
            long amperage = energyContainer.getOutputAmperage();
            if (amperage == 1) {
                // Amperage is 1 when the energy is not exactly on a tier.
                // The voltage for recipe search is always on tier, so take the closest lower tier.
                // List check is done because single hatches will always be a "clean voltage," no need
                // for any additional checks.
                return EFValues.VEX[GTUtil.getFloorTierByVoltage(voltage)];
            } else {
                return voltage;
            }
        } else {
            // Machines
            long highestVoltage = energyContainer.getHighestInputVoltage();
            if (energyContainer.getNumHighestInputContainers() > 1) {
                // allow tier + 1 if there are multiple hatches present at the highest tier
                int tier = GTUtil.getTierByVoltage(highestVoltage);
                return EFValues.V[Math.min(tier + 1, EFValues.MAX)];
            } else {
                return highestVoltage;
            }
        }
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return Math.max(this.getEnergyContainer().getHighestInputVoltage(),
                this.getEnergyContainer().getOutputVoltage());
    }

    /**
     * Is this multiblock a generator?
     * Used for max voltage calculations.
     */
    public boolean isGenerator() {
        return getDefinition().isGenerator();
    }
}
