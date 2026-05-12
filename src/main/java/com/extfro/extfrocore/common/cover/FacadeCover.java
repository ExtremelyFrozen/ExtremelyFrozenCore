package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.item.behavior.FacadeItemBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class FacadeCover extends CoverBehavior {

    @Getter
    @SyncToClient
    @SaveField
    @RerenderOnChanged
    private BlockState facadeState = Blocks.STONE.defaultBlockState();

    public FacadeCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
        this.facadeState = FacadeItemBehaviour.getFacadeState(itemStack);
    }

    @Override
    public boolean shouldRenderPlate() {
        return facadeState.canOcclude();
    }

    public void setFacadeState(BlockState state) {
        facadeState = state;
        syncDataHolder.markClientSyncFieldDirty("facadeState");
    }

    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean canPipePassThrough() {
        return false;
    }

    @Nullable
    public BlockState getAppearance(@Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return facadeState;
    }
}
