package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.ModularUIBuilder;
import com.extfro.extfrocore.api.gui.UITemplate;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.gui.widget.ToggleButtonWidget;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.common.data.GTMaterials;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidType;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class PumpHatchPartMachine extends FluidHatchPartMachine {

    public PumpHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.OUT, FluidType.BUCKET_VOLUME, 1);
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return super.createTank(initialCapacity, slots)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Water.getFluidTag()));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUIBuilder(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(MachineUIHelper.image(7, 16, 81, 55, GuiTextures.DISPLAY))
                .widget(MachineUIHelper.label(11, 20, "gtceu.gui.fluid_amount"))
                .widget(MachineUIHelper.lightLabel(11, 30,
                        () -> net.minecraft.network.chat.Component.literal(
                                String.valueOf(tank.getFluidInTank(0).getAmount()))))
                .widget(MachineUIHelper.label(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, io.support(IO.IN))
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_input.tooltip"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO.
    @Override
    public boolean swapIO() {
        return false;
    }
}
