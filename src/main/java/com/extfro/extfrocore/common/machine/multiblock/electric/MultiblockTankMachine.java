package com.extfro.extfrocore.common.machine.multiblock.electric;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.fluids.PropertyFluidFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.widget.TankWidget;
import com.extfro.extfrocore.api.machine.feature.IFancyUIMachine;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.machine.trait.NotifiableFluidTank;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.common.machine.gui.MachineUIHelper;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class MultiblockTankMachine extends MultiblockControllerMachine implements IFancyUIMachine {

    @SaveField
    @Getter
    private final NotifiableFluidTank tank;

    public MultiblockTankMachine(BlockEntityCreationInfo info, int capacity, @Nullable PropertyFluidFilter filter) {
        super(info);

        this.tank = attachTrait(new NotifiableFluidTank(1, capacity, IO.BOTH));
        if (filter != null) tank.setFilter(filter);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        var superResult = super.onUse(context);

        if (superResult != InteractionResult.PASS) return superResult;
        if (!isFormed()) return InteractionResult.FAIL;

        return InteractionResult.PASS; // Otherwise let MetaMachineBlock.use() open the UI
    }

    @Override
    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (isFormed) {
            return super.getFluidHandlerCap(side, useCoverCapability);
        }
        return null;
    }

    /////////////////////////////////////
    // *********** GUI ***********//
    /////////////////////////////////////

    @Override
    public UIElement createUIWidget() {
        var group = MachineUIHelper.group(90, 63)
                .style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));

        group.addChild(MachineUIHelper.image(4, 4, 82, 55, GuiTextures.DISPLAY));
        group.addChild(MachineUIHelper.label(8, 8, "gtceu.gui.fluid_amount"));
        group.addChild(MachineUIHelper.lightLabel(8, 18, () -> Component.literal(getFluidLabel())));
        group.addChild(new TankWidget(tank.getStorages()[0], 68, 23, true, true)
                .setBackground(GuiTextures.FLUID_SLOT));

        return group;
    }

    private String getFluidLabel() {
        return String.valueOf(tank.getFluidInTank(0).getAmount());
    }
}
