package com.extfro.extfrocore.common.machine.multiblock.electric;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.IEnergyContainer;
import com.extfro.extfrocore.api.capability.IMiner;
import com.extfro.extfrocore.api.capability.recipe.EURecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.FluidRecipeCapability;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.machine.feature.IDataInfoProvider;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.extfro.extfrocore.api.misc.EnergyContainerList;
import com.extfro.extfrocore.api.transfer.fluid.FluidHandlerList;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.common.data.GTMaterials;
import com.extfro.extfrocore.common.item.behavior.PortableScannerBehavior;
import com.extfro.extfrocore.common.machine.trait.miner.LargeMinerLogic;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;
import com.extfro.extfrocore.utils.GTTransferUtils;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import com.lowdragmc.lowdraglib2.gui.util.ClickData;
import com.lowdragmc.lowdraglib2.gui.widget.ComponentPanelWidget;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.extfro.extfrocore.common.data.GTMaterials.DrillingFluid;

public class LargeMinerMachine extends WorkableElectricMultiblockMachine
                               implements IMiner, IControllable, IDataInfoProvider {

    public static final int CHUNK_LENGTH = 16;
    @Getter
    private final int tier;
    @Nullable
    protected EnergyContainerList energyContainer;
    @Nullable
    protected FluidHandlerList inputFluidInventory;
    private final int drillingFluidConsumePerTick;

    public LargeMinerMachine(BlockEntityCreationInfo info, int tier, int speed, int maximumChunkDiameter, int fortune,
                             int drillingFluidConsumePerTick) {
        super(info, new LargeMinerLogic(fortune, speed, maximumChunkDiameter * CHUNK_LENGTH / 2));
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
    }

    @Override
    public LargeMinerLogic getRecipeLogic() {
        return (LargeMinerLogic) super.getRecipeLogic();
    }

    public static Material getMaterial(int tier) {
        if (tier == EFValues.EV) return GTMaterials.Steel;
        if (tier == EFValues.IV) return GTMaterials.Titanium;
        if (tier == EFValues.LuV) return GTMaterials.TungstenSteel;
        return GTMaterials.Steel;
    }

    public static net.minecraft.world.level.block.Block getCasingState(int tier) {
        return GTBlocks.MATERIALS_TO_CASINGS.get(getMaterial(tier)).get();
    }

    public long getMaxVoltage() {
        return EFValues.V[getEnergyTier()];
    }

    //////////////////////////////////////
    // ******* Logic *********//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Direction opposite = this.getUpwardsFacing().getOpposite();
        getRecipeLogic().setDir(opposite == Direction.NORTH ? Direction.UP : Direction.DOWN);
        initializeAbilities();
    }

    @Override
    public boolean checkPattern() {
        return super.checkPattern() &&
                (this.getUpwardsFacing() == Direction.NORTH || this.getUpwardsFacing() == Direction.SOUTH);
    }

    private void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IFluidHandler> fluidTanks = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(fluidTanks::add);
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.inputFluidInventory = new FluidHandlerList(fluidTanks);

        getRecipeLogic().setVoltageTier(GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()));
        getRecipeLogic().setOverclockAmount(
                Math.max(1, GTUtil.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        getRecipeLogic().initPos(getBlockPos(), getRecipeLogic().getCurrentRadius());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1,
                Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainInput(boolean simulate) {
        // drain energy
        if (energyContainer != null && energyContainer.getEnergyStored() > 0) {
            long energyToDrain = EFValues.VA[getEnergyTier()];
            long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                if (!simulate) {
                    energyContainer.changeEnergy(-energyToDrain);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }

        // drain fluid
        if (inputFluidInventory != null && inputFluidInventory.handlers.length > 0) {
            FluidStack drillingFluid = DrillingFluid
                    .getFluid(this.drillingFluidConsumePerTick * getRecipeLogic().getOverclockAmount());
            FluidStack fluidStack = inputFluidInventory.getFluidInTank(0);
            if (fluidStack != FluidStack.EMPTY && fluidStack.is(DrillingFluid.getFluid()) &&
                    fluidStack.getAmount() >= drillingFluid.getAmount()) {
                if (!simulate) {
                    GTTransferUtils.drainFluidAccountNotifiableList(inputFluidInventory, drillingFluid,
                            IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            int workingAreaChunks = getRecipeLogic().getCurrentRadius() * 2 / CHUNK_LENGTH;
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            textList.add(Component.translatable("gtceu.machine.miner.startx",
                    getRecipeLogic().getX() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getX()));
            textList.add(Component.translatable("gtceu.machine.miner.starty",
                    getRecipeLogic().getY() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getY()));
            textList.add(Component.translatable("gtceu.machine.miner.startz",
                    getRecipeLogic().getZ() == Integer.MAX_VALUE ? 0 : getRecipeLogic().getZ()));
            textList.add(Component.translatable("gtceu.universal.tooltip.silk_touch")
                    .append(ComponentPanelWidget.withButton(Component.literal("[")
                            .append(getRecipeLogic().isSilkTouchMode() ?
                                    Component.translatable("gtceu.creative.activity.on") :
                                    Component.translatable("gtceu.creative.activity.off"))
                            .append(Component.literal("]")), "silk_touch")));
            textList.add(Component.translatable("gtceu.universal.tooltip.chunk_mode")
                    .append(ComponentPanelWidget.withButton(Component.literal("[")
                            .append(getRecipeLogic().isChunkMode() ?
                                    Component.translatable("gtceu.creative.activity.on") :
                                    Component.translatable("gtceu.creative.activity.off"))
                            .append(Component.literal("]")), "chunk_mode")));
            if (getRecipeLogic().isChunkMode()) {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area_chunks", workingAreaChunks,
                        workingAreaChunks));
            } else {
                textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            if (getRecipeLogic().isDone()) {
                textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            }
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("chunk_mode")) {
                getRecipeLogic().setChunkMode(!getRecipeLogic().isChunkMode());
            }
            if (componentData.equals("silk_touch")) {
                getRecipeLogic().setSilkTouchMode(!getRecipeLogic().isSilkTouchMode());
            }
        }
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (isRemote() || !this.isFormed())
            return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = getRecipeLogic().getCurrentRadius();
            if (getRecipeLogic().isChunkMode()) {
                if (currentRadius - CHUNK_LENGTH <= 0) {
                    getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
                } else {
                    getRecipeLogic().setCurrentRadius(currentRadius - CHUNK_LENGTH);
                }
                int workingAreaChunks = getRecipeLogic().getCurrentRadius() * 2 / CHUNK_LENGTH;
                context.getPlayer()
                        .sendSystemMessage(Component.translatable("gtceu.universal.tooltip.working_area_chunks",
                                workingAreaChunks, workingAreaChunks));
            } else {
                if (currentRadius - CHUNK_LENGTH / 2 <= 0) {
                    getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
                } else {
                    getRecipeLogic().setCurrentRadius(currentRadius - CHUNK_LENGTH / 2);
                }
                int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
                context.getPlayer().sendSystemMessage(
                        Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
            }
            getRecipeLogic().resetArea(true);
        } else {
            context.getPlayer().sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

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
