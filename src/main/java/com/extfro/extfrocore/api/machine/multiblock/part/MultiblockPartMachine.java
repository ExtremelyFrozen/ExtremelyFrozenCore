package com.extfro.extfrocore.api.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.machine.property.MachineModelProperties;
import com.extfro.extfrocore.api.sync_system.annotations.ClientFieldChangeListener;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

public class MultiblockPartMachine extends MetaMachine implements IMultiPart {

    @SyncToClient
    @RerenderOnChanged
    protected final Set<BlockPos> controllerPositions = new ObjectOpenHashSet<>(8);
    protected final SortedSet<MultiblockControllerMachine> controllers = new ReferenceLinkedOpenHashSet<>(8);

    public MultiblockPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean hasController(BlockPos controllerPos) {
        return controllerPositions.contains(controllerPos);
    }

    @Override
    public boolean isFormed() {
        return !controllerPositions.isEmpty();
    }

    @ClientFieldChangeListener(fieldName = "controllerPositions")
    public void onControllersUpdated() {
        controllers.clear();
        for (BlockPos controllerPos : controllerPositions) {
            if (MetaMachine.getMachine(getLevel(), controllerPos) instanceof MultiblockControllerMachine controller) {
                controllers.add(controller);
            }
        }
    }

    @Override
    @UnmodifiableView
    public SortedSet<MultiblockControllerMachine> getControllers() {
        if (controllers.size() != controllerPositions.size()) {
            onControllersUpdated();
        }
        return Collections.unmodifiableSortedSet(controllers);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            Set<MultiblockControllerMachine> toIterate = controllers.size() > 1 ? new ObjectOpenHashSet<>(controllers) :
                    controllers;
            for (MultiblockControllerMachine controller : toIterate) {
                if (serverLevel.isLoaded(controller.getBlockPos())) {
                    removedFromController(controller);
                    controller.onPartUnload();
                }
            }
        }
        controllerPositions.clear();
        controllers.clear();
    }

    @Override
    @MustBeInvokedByOverriders
    public void removedFromController(MultiblockControllerMachine controller) {
        controllerPositions.remove(controller.getBlockPos());
        controllers.remove(controller);
        if (controllers.isEmpty()) {
            var renderState = getRenderState();
            if (renderState.hasProperty(MachineModelProperties.IS_FORMED)) {
                setRenderState(renderState.setValue(MachineModelProperties.IS_FORMED, false));
            }
        }
        syncDataHolder.resyncAllFields();
        markAsChanged();
    }

    @Override
    @MustBeInvokedByOverriders
    public void addedToController(MultiblockControllerMachine controller) {
        controllerPositions.add(controller.getBlockPos());
        controllers.add(controller);
        var renderState = getRenderState();
        if (renderState.hasProperty(MachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(MachineModelProperties.IS_FORMED, true));
        }
        syncDataHolder.resyncAllFields();
        markAsChanged();
    }

    @Override
    public boolean replacePartModelWhenFormed() {
        var renderState = getRenderState();
        return renderState.hasProperty(MachineModelProperties.IS_FORMED) &&
                renderState.getValue(MachineModelProperties.IS_FORMED);
    }

    @Override
    public @Nullable BlockState getFormedAppearance(BlockState sourceState, BlockPos sourcePos, Direction side) {
        if (!replacePartModelWhenFormed()) {
            return null;
        }
        return IMultiPart.super.getFormedAppearance(sourceState, sourcePos, side);
    }
}
