package com.extfro.extfrocore.api.machine.multiblock;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public abstract class MultiblockControllerMachine extends MetaMachine {

    public MultiblockControllerMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public MultiblockMachineDefinition getDefinition() {
        return (MultiblockMachineDefinition) super.getDefinition();
    }

    public boolean allowFlip() {
        return getDefinition().isAllowFlip();
    }

    public java.util.Comparator<IMultiPart> getPartSorter() {
        return getDefinition().getPartSorter().apply(this);
    }

    @Nullable
    public BlockState getPartAppearance(IMultiPart part, Direction side, BlockState sourceState, net.minecraft.core.BlockPos sourcePos) {
        return getDefinition().getPartAppearance().apply(this, part, side);
    }
}
