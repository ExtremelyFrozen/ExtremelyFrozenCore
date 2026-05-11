package com.extfro.extfrocore.api.cover;

import com.extfro.extfrocore.api.blockentity.ICopyable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.gui.factory.CoverUIFactory;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.sync_system.ISyncManaged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.sync_system.holder.SyncDataHolder;
import com.extfro.extfrocore.api.tool.EFToolType;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.client.renderer.cover.ICoverRenderer;
import com.extfro.extfrocore.client.renderer.cover.IDynamicCoverRenderer;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class CoverBehavior implements ISyncManaged, ICopyable {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    public final CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    @Getter
    @SaveField
    @SyncToClient
    protected ItemStack attachItem = ItemStack.EMPTY;
    @Getter
    @SaveField
    protected int redstoneSignalOutput = 0;

    public CoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        this.coverDefinition = definition;
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }

    @Override
    public void scheduleRenderUpdate() {
        coverHolder.scheduleRenderUpdate();
    }

    @Override
    public void markAsChanged() {
        coverHolder.markAsChanged();
    }

    @MustBeInvokedByOverriders
    public boolean canAttach() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getBlockPos());
        return machine == null ||
                (machine.getDefinition().isAllowCoverOnFront() || !machine.hasFrontFacing() ||
                        coverHolder.getFrontFacing() != attachedSide);
    }

    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        attachItem = itemStack.copy();
        attachItem.setCount(1);
        syncDataHolder.resyncAllFields();
    }

    public void setAttachItem(ItemStack attachItem) {
        this.attachItem = attachItem.copy();
        if (!this.attachItem.isEmpty()) {
            this.attachItem.setCount(1);
        }
        syncDataHolder.resyncAllFields();
    }

    public void onLoad() {}

    public void onUnload() {}

    public ItemStack getPickItem() {
        return attachItem;
    }

    public List<ItemStack> getAdditionalDrops() {
        return new ArrayList<>();
    }

    public void onRemoved() {}

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}

    public void setRedstoneSignalOutput(int redstoneSignalOutput) {
        if (this.redstoneSignalOutput == redstoneSignalOutput) return;
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
    }

    public boolean canConnectRedstone() {
        return false;
    }

    public final Pair<EFToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        var toolType = context.getToolType();
        if (toolType.contains(EFToolType.SCREWDRIVER)) {
            return Pair.of(EFToolType.SCREWDRIVER, onScrewdriverClick(context));
        }
        if (toolType.contains(EFToolType.SOFT_MALLET)) {
            return Pair.of(EFToolType.SOFT_MALLET, onSoftMalletClick(context));
        }
        return Pair.of(null, InteractionResult.PASS);
    }

    public InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (this instanceof IUICover && context.getPlayer() instanceof ServerPlayer serverPlayer) {
            return CoverUIFactory.INSTANCE.openUI(this, serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    public boolean canPipePassThrough() {
        return true;
    }

    public boolean shouldRenderPlate() {
        return true;
    }

    public @Nullable Supplier<ICoverRenderer> getCoverRenderer() {
        return coverDefinition.getCoverRenderer();
    }

    @Nullable
    public BlockState getAppearance(@Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return null;
    }

    public Supplier<IDynamicCoverRenderer> getDynamicRenderer() {
        return () -> null;
    }

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return defaultValue;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return defaultValue;
    }
}
