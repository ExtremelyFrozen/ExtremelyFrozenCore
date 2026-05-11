package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.blockentity.ITickSubscription;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.data.RotationState;
import com.extfro.extfrocore.api.machine.feature.IMachineFeature;
import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;
import com.extfro.extfrocore.api.sync_system.ManagedSyncBlockEntity;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.tool.EFToolType;
import com.extfro.extfrocore.api.tool.ToolHelper;
import com.extfro.extfrocore.api.transfer.fluid.IFluidHandlerModifiable;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MetaMachine extends ManagedSyncBlockEntity implements ITickSubscription, IMachineFeature {

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
        if (this instanceof IRecipeLogicMachine recipeLogicMachine) {
            recipeLogicMachine.getRecipeLogic().onMachineLoad();
        }
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

    public void clientTick() {
        pushClientChangesToServer();
    }

    public final Pair<EFToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return Pair.of(null, InteractionResult.PASS);
        }
        var toolType = context.getToolType();
        Pair<EFToolType, InteractionResult> result = null;

        var cover = coverContainer.getCoverAtSide(context.getClickedFace());
        if (cover != null) {
            result = cover.onToolClick(context);
            if (result.getSecond() != InteractionResult.PASS) {
                return result;
            }
            if (toolType.contains(EFToolType.CROWBAR) && !isRemote()) {
                coverContainer.removeCover(true, context.getGridSide(), player);
                return Pair.of(EFToolType.CROWBAR, InteractionResult.SUCCESS);
            }
        }

        if (toolType.contains(EFToolType.SCREWDRIVER)) {
            result = Pair.of(EFToolType.SCREWDRIVER, onScrewdriverClick(context));
        } else if (toolType.contains(EFToolType.SOFT_MALLET)) {
            result = Pair.of(EFToolType.SOFT_MALLET, onSoftMalletClick(context));
        } else if (toolType.contains(EFToolType.WRENCH)) {
            result = Pair.of(EFToolType.WRENCH, onWrenchClick(context));
        } else if (toolType.contains(EFToolType.CROWBAR)) {
            result = Pair.of(EFToolType.CROWBAR, onCrowbarClick(context));
        } else if (toolType.contains(EFToolType.HARD_HAMMER)) {
            result = Pair.of(EFToolType.HARD_HAMMER, onHardHammerClick(context));
        }
        return result != null ? result : Pair.of(null, InteractionResult.PASS);
    }

    protected InteractionResult onHardHammerClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onCrowbarClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onWrenchClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        if (!itemStack.isEmpty()) {
            var coverDefinition = CoverDefinition.getForItem(itemStack);
            if (coverDefinition.isPresent()) {
                Direction side = context.getGridSide();
                if (!isRemote() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer &&
                        coverContainer.placeCoverOnSide(side, itemStack, coverDefinition.get(), serverPlayer) &&
                        !serverPlayer.isCreative()) {
                    itemStack.shrink(1);
                }
                return InteractionResult.sidedSuccess(isRemote());
            }
        }
        var types = context.getToolType();
        if ((!types.isEmpty() && ToolHelper.canUse(context.getItemInHand())) ||
                (types.isEmpty() && player != null && player.isShiftKeyDown())) {
            InteractionResult result = onToolClick(context).getSecond();
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onUse(ExtendedUseOnContext context) {
        var player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            var cover = coverContainer.getCoverAtSide(context.getClickedFace());
            if (cover != null) {
                InteractionResult result = cover.onScrewdriverClick(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

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

    public Direction getUpwardsFacing() {
        if (getDefinition().isAllowExtendedFacing() &&
                getBlockState().hasProperty(com.extfro.extfrocore.api.block.property.EFBlockStateProperties.UPWARDS_FACING)) {
            return getBlockState().getValue(com.extfro.extfrocore.api.block.property.EFBlockStateProperties.UPWARDS_FACING);
        }
        Direction frontFacing = getFrontFacing();
        return frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
    }

    public boolean isFacingValid(Direction facing) {
        return getDefinition().getRotationState().test(facing);
    }

    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(getDefinition().getShape(getFrontFacing()));
    }

    public boolean canConnectRedstone(@Nullable Direction side) {
        if (side != null) {
            var cover = coverContainer.getCoverAtSide(side.getOpposite());
            return cover != null && cover.canConnectRedstone();
        }
        return coverContainer.getCovers().stream().anyMatch(cover -> cover.canConnectRedstone());
    }

    public int getOutputSignal(Direction direction) {
        var cover = coverContainer.getCoverAtSide(direction.getOpposite());
        return cover == null ? 0 : cover.getRedstoneSignalOutput();
    }

    public int getOutputDirectSignal(Direction direction) {
        return getOutputSignal(direction);
    }

    public int getAnalogOutputSignal() {
        return 0;
    }

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        IItemHandlerModifiable handler = getRawItemHandlerCap(side);
        if (handler == null || !useCoverCapability || side == null) {
            return handler;
        }
        var cover = coverContainer.getCoverAtSide(side);
        return cover == null ? handler : cover.getItemHandlerCap(handler);
    }

    @Nullable
    protected IItemHandlerModifiable getRawItemHandlerCap(@Nullable Direction side) {
        return null;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        IFluidHandlerModifiable handler = getRawFluidHandlerCap(side);
        if (handler == null || !useCoverCapability || side == null) {
            return handler;
        }
        var cover = coverContainer.getCoverAtSide(side);
        return cover == null ? handler : cover.getFluidHandlerCap(handler);
    }

    @Nullable
    protected IFluidHandlerModifiable getRawFluidHandlerCap(@Nullable Direction side) {
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
    public static MetaMachine getMachine(@Nullable BlockGetter level, BlockPos pos) {
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MetaMachine machine ? machine : null;
    }
}
