package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.UITemplate;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.common.data.GTMaterials;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidType;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class SteamHatchPartMachine extends FluidHatchPartMachine {

    public static final int INITIAL_TANK_CAPACITY = 64 * FluidType.BUCKET_VOLUME;
    public static final boolean IS_STEEL = ConfigHolder.INSTANCE.machines.steelSteamMultiblocks;

    public SteamHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.IN, SteamHatchPartMachine.INITIAL_TANK_CAPACITY, 1);
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return super.createTank(initialCapacity, slots)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(IS_STEEL))
                .widget(MachineUIHelper.image(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(IS_STEEL)))
                .widget(MachineUIHelper.label(11, 20, "gtceu.gui.fluid_amount"))
                .widget(MachineUIHelper.lightLabel(11, 30,
                        () -> net.minecraft.network.chat.Component.literal(tank.getFluidInTank(0).getAmount() + "")))
                .widget(MachineUIHelper.label(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(IS_STEEL), 7, 84, true));
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO, since this is a
    // hatch that only allows steam in, not
    // a steam version of an input/output hatch
    @Override
    public boolean swapIO() {
        return false;
    }
}
