package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.blockentity.ITickSubscription;
import com.extfro.extfrocore.api.data.RotationState;
import com.extfro.extfrocore.api.sync_system.ManagedSyncBlockEntity;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MetaMachine extends ManagedSyncBlockEntity implements ITickSubscription {

    @Getter
    private MachineRenderState renderState;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private final MachineCoverContainer coverContainer = new MachineCoverContainer(this);
    private final List<TickableSubscription> serverTicks = new ArrayList<>();

    public MetaMachine(BlockEntityCreationInfo info) {
        super(info);
        MachineRenderState defaultState = getDefinition().defaultRenderState();
        renderState = defaultState;
    }

    public MachineDefinition getDefinition() {
        if (getBlockState().getBlock() instanceof com.extfro.extfrocore.api.block.MetaMachineBlock block) {
            return block.getDefinition();
        }
        throw new IllegalStateException("Machine block entity is not attached to a machine block");
    }

    public void setRenderState(MachineRenderState renderState) {
        this.renderState = renderState;
        scheduleRenderUpdate();
    }

    public boolean isRemote() {
        return getLevel() != null && getLevel().isClientSide;
    }

    public void onLoad() {
        coverContainer.onLoad();
    }

    public void onUnload() {
        coverContainer.onUnload();
    }

    @Override
    public final void setRemoved() {
        super.setRemoved();
        onUnload();
    }

    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {}

    public void onMachineDestroyed() {
        coverContainer.dropAllCovers();
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {}

    public void modifyDrops(List<ItemStack> drops) {
        for (var cover : coverContainer.getCovers()) {
            drops.add(cover.getPickItem());
            drops.addAll(cover.getAdditionalDrops());
        }
    }

    public void animateTick(net.minecraft.util.RandomSource random) {}

    public void serverTick() {
        serverTicks.removeIf(subscription -> {
            if (subscription.isStillSubscribed()) {
                subscription.run();
                return false;
            }
            return true;
        });
    }

    public void clientTick() {}

    public void scheduleRenderUpdate() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    public void notifyBlockUpdate() {
        markAsChanged();
    }

    public void scheduleNeighborShapeUpdate() {
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public long getOffsetTimer() {
        return level == null ? 0L : level.getGameTime();
    }

    public boolean hasFrontFacing() {
        return getDefinition().getRotationState() != RotationState.NONE;
    }

    public Direction getFrontFacing() {
        RotationState rotationState = getDefinition().getRotationState();
        if (rotationState == RotationState.NONE || !getBlockState().hasProperty(rotationState.property)) {
            return Direction.NORTH;
        }
        return getBlockState().getValue(rotationState.property);
    }

    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(getDefinition().getShape(getFrontFacing()));
    }

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public @Nullable TickableSubscription subscribeServerTick(Runnable runnable) {
        TickableSubscription subscription = new TickableSubscription(runnable);
        serverTicks.add(subscription);
        return subscription;
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    @Nullable
    public static MetaMachine getMachine(@Nullable Level level, BlockPos pos) {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MetaMachine machine ? machine : null;
    }
}
