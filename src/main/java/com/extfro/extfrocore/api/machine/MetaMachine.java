package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.sync_system.ManagedSyncBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MetaMachine extends ManagedSyncBlockEntity {

    @Getter
    private MachineRenderState renderState;

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

    public void onLoad() {}

    public void onUnload() {}

    @Override
    public final void setRemoved() {
        super.setRemoved();
        onUnload();
    }

    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {}

    public void onMachineDestroyed() {}

    public void onRotated(Direction oldFacing, Direction newFacing) {}

    public void modifyDrops(List<ItemStack> drops) {}

    public void animateTick(net.minecraft.util.RandomSource random) {}

    public void serverTick() {}

    public void clientTick() {}

    public void scheduleRenderUpdate() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    @Nullable
    public static MetaMachine getMachine(@Nullable Level level, BlockPos pos) {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MetaMachine machine ? machine : null;
    }
}
