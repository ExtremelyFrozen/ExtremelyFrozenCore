package com.extfro.extfrocore.api.machine.feature.multiblock;

import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.SortedSet;

public interface IMultiPart {

    default boolean canShared() {
        return true;
    }

    boolean hasController(BlockPos controllerPos);

    boolean isFormed();

    @UnmodifiableView
    SortedSet<MultiblockControllerMachine> getControllers();

    void removedFromController(MultiblockControllerMachine controller);

    void addedToController(MultiblockControllerMachine controller);

    default boolean replacePartModelWhenFormed() {
        return true;
    }

    @Nullable
    default BlockState getFormedAppearance(BlockState sourceState, BlockPos sourcePos, Direction side) {
        for (MultiblockControllerMachine controller : getControllers()) {
            var appearance = controller.getPartAppearance(this, side, sourceState, sourcePos);
            if (appearance != null) return appearance;
        }
        return null;
    }
}
