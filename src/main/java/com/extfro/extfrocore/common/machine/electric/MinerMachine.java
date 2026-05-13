package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.IMiner;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.TickableSubscription;
import com.extfro.extfrocore.api.machine.WorkableTieredMachine;
import com.extfro.extfrocore.api.machine.feature.IDataInfoProvider;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.trait.AutoOutputTrait;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.item.behavior.PortableScannerBehavior;
import com.extfro.extfrocore.common.machine.trait.miner.MinerLogic;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.ISubscription;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinerMachine extends WorkableTieredMachine
                          implements IControllable, IFancyUIMachine, IDataInfoProvider, IMiner {

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    private final long energyPerTick;
    @Nullable
    protected TickableSubscription batterySubs;
    @Nullable
    protected ISubscription energySubs;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public MinerMachine(BlockEntityCreationInfo info, int tier, int speed, int maximumRadius, int fortune) {
        super(info, tier,
                new MinerLogic(fortune, speed, maximumRadius),
                0, (tier + 1) * (tier + 1), 0, 0, ($) -> 0);
        this.energyPerTick = EFValues.V[tier - 1];
        this.chargerInventory = createChargerItemHandler();
        this.autoOutput = attachTrait(AutoOutputTrait.ofItems(exportItems));
        autoOutput.setItemOutputDirectionValidator(d -> d != Direction.DOWN);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected CustomItemStackHandler createChargerItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        // Remove the miner pipes below this miner
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public MinerLogic getRecipeLogic() {
        return (MinerLogic) super.getRecipeLogic();
    }

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

    //////////////////////////////////////
    // ********** LOGIC **********//
    //////////////////////////////////////
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
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        int rowSize = (int) Math.sqrt(exportItems.getSlots());
        int templateWidth = rowSize * 18 + 120;
        int templateHeight = Math.max(rowSize * 18, 80);

        UIElement template = new UIElement().layout(layout -> layout.width(templateWidth).height(templateHeight));
        UIElement slots = new UIElement().layout(layout -> layout.left(120)
                .top((templateHeight - rowSize * 18) / 2f).width(rowSize * 18).height(rowSize * 18));
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                slots.addChild(itemSlot(exportItems, index, x * 18, y * 18, GuiTextures.SLOT, true, false));
            }
        }

        UIElement infoPanel = panel(117, templateHeight, GuiTextures.BACKGROUND_INVERSE);
        ScrollerView textScroller = new ScrollerView();
        textScroller.layout(layout -> layout.left(4).top(4).width(109).height(templateHeight - 8));
        textScroller.style(style -> style.background(GuiTextures.DISPLAY));
        List<Component> displayText = new ArrayList<>();
        addDisplayText(displayText);
        int lineY = 2;
        for (Component component : displayText) {
            int top = lineY;
            Label line = new Label();
            line.setValue(component);
            line.layout(layout -> layout.left(2).top(top).width(102).height(10));
            line.textStyle(style -> style.textColor(0x404040).textShadow(false));
            line.style(style -> style.tooltips(component));
            textScroller.addScrollViewChild(line);
            lineY += 10;
        }
        infoPanel.addChild(textScroller);
        template.addChild(infoPanel);
        template.addChild(slots);

        UIElement energyGroup = new UIElement().layout(layout -> layout.width(18).height(79));
        energyGroup.addChild(createEnergyBar(this));
        energyGroup.addChild(itemSlot(chargerInventory, 0, 0, 61,
                new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY), true, true,
                Component.translatable("gtceu.gui.charger_slot.tooltip", EFValues.VNF[getTier()],
                        EFValues.VNF[getTier()])));

        int groupWidth = Math.max(templateWidth + 12, 172);
        int groupHeight = templateHeight + 8;
        UIElement group = new UIElement().layout(layout -> layout.width(groupWidth).height(groupHeight));
        energyGroup.layout(layout -> layout.left(3).top((groupHeight - 79) / 2f).width(18).height(79));
        template.layout(layout -> layout.left((groupWidth - 4 - templateWidth) / 2f + 4)
                .top((groupHeight - templateHeight) / 2f).width(templateWidth).height(templateHeight));
        group.addChild(energyGroup);
        group.addChild(template);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
        textList.add(recipeLogic.getCustomProgressLine());
        textList.add(Component.translatable("gtceu.machine.miner.startx", getRecipeLogic().getX()).append(" ")
                .append(Component.translatable("gtceu.machine.miner.minex", getRecipeLogic().getMineX())));
        textList.add(Component.translatable("gtceu.machine.miner.starty", getRecipeLogic().getY()).append(" ")
                .append(Component.translatable("gtceu.machine.miner.miney", getRecipeLogic().getMineY())));
        textList.add(Component.translatable("gtceu.machine.miner.startz", getRecipeLogic().getZ()).append(" ")
                .append(Component.translatable("gtceu.machine.miner.minez", getRecipeLogic().getMineZ())));
        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        if (getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        else if (getRecipeLogic().isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (getRecipeLogic().isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    @Override
    public boolean drainInput(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (isRemote()) return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = getRecipeLogic().getCurrentRadius();
            if (currentRadius == 1)
                getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
            else if (context.getPlayer().isShiftKeyDown())
                getRecipeLogic().setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                getRecipeLogic().setCurrentRadius(Math.max(1, currentRadius - 1));

            getRecipeLogic().resetArea(true);

            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            context.getPlayer().sendSystemMessage(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        } else {
            context.getPlayer().sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        }
        return new ArrayList<>();
    }
}
