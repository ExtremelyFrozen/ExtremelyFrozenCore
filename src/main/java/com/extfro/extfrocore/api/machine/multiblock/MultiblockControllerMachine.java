package com.extfro.extfrocore.api.machine.multiblock;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.property.MachineModelProperties;
import com.extfro.extfrocore.api.pattern.BlockPattern;
import com.extfro.extfrocore.api.pattern.MultiblockState;
import com.extfro.extfrocore.api.sync_system.annotations.ClientFieldChangeListener;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class MultiblockControllerMachine extends MetaMachine {

    private MultiblockState multiblockState;
    private final List<IMultiPart> parts = new ArrayList<>();
    @Getter
    @SyncToClient
    private BlockPos[] partPositions = new BlockPos[0];
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean isFormed;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean isFlipped;

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

    public MultiblockState getMultiblockState() {
        if (multiblockState == null) {
            multiblockState = new MultiblockState(getLevel(), getBlockPos());
        }
        return multiblockState;
    }

    public BlockPattern getPattern() {
        return getDefinition().getPatternFactory().get();
    }

    public Comparator<IMultiPart> getPartSorter() {
        return getDefinition().getPartSorter().apply(this);
    }

    @Nullable
    public BlockState getPartAppearance(IMultiPart part, Direction side, BlockState sourceState, BlockPos sourcePos) {
        return isFormed() ? getDefinition().getPartAppearance().apply(this, part, side) : null;
    }

    public List<IMultiPart> getParts() {
        if (parts.size() != partPositions.length) {
            onPartsUpdated();
        }
        return Collections.unmodifiableList(parts);
    }

    @ClientFieldChangeListener(fieldName = "partPositions")
    protected void onPartsUpdated() {
        parts.clear();
        for (BlockPos pos : partPositions) {
            if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                parts.add(part);
            }
        }
    }

    protected void updatePartPositions() {
        partPositions = parts.isEmpty() ? new BlockPos[0] :
                parts.stream().map(part -> part.self().getBlockPos()).toArray(BlockPos[]::new);
        syncDataHolder.resyncAllFields();
        markAsChanged();
    }

    public boolean shouldAddPartToController(IMultiPart part) {
        return true;
    }

    public void onStructureFormed() {
        isFormed = true;
        var renderState = getRenderState();
        if (renderState.hasProperty(MachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(MachineModelProperties.IS_FORMED, true));
        }
        parts.clear();
        Set<IMultiPart> matchedParts = getMultiblockState().getMatchContext().getOrCreate("parts", Set::of);
        for (IMultiPart part : matchedParts) {
            if (shouldAddPartToController(part)) {
                parts.add(part);
            }
        }
        parts.sort(getPartSorter());
        updatePartPositions();
        for (IMultiPart part : parts) {
            part.addedToController(this);
        }
        updatePartPositions();
    }

    public void onStructureInvalid() {
        isFormed = false;
        var renderState = getRenderState();
        if (renderState.hasProperty(MachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(MachineModelProperties.IS_FORMED, false));
        }
        for (IMultiPart part : parts) {
            part.removedFromController(this);
        }
        parts.clear();
        updatePartPositions();
    }

    public void onPartUnload() {
        parts.removeIf(part -> part.self().isRemoved());
        getMultiblockState().setError(MultiblockState.UNLOAD_ERROR);
        updatePartPositions();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        if (oldFacing != newFacing && getLevel() instanceof ServerLevel) {
            onStructureInvalid();
        }
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
        syncDataHolder.resyncAllFields();
        markAsChanged();
    }

    public boolean checkPattern() {
        BlockPattern pattern = getPattern();
        return pattern != null && pattern.checkPatternAt(getMultiblockState(), false);
    }

    public boolean checkPatternWithLock() {
        return checkPattern();
    }

    public boolean checkPatternWithTryLock() {
        return checkPattern();
    }
}
