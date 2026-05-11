package com.extfro.extfrocore.api.block;

import com.extfro.extfrocore.api.block.property.EFBlockStateProperties;
import com.extfro.extfrocore.api.data.RotationState;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaMachineBlock extends Block implements EntityBlock {

    private final MachineDefinition definition;

    public MetaMachineBlock(Properties properties, MachineDefinition definition) {
        super(properties);
        this.definition = definition;
        RotationState rotationState = definition.getRotationState();
        if (rotationState != RotationState.NONE) {
            BlockState defaultState = defaultBlockState().setValue(rotationState.property, rotationState.defaultDirection);
            if (definition.isAllowExtendedFacing()) {
                defaultState = defaultState.setValue(EFBlockStateProperties.UPWARDS_FACING, Direction.NORTH);
            }
            registerDefaultState(defaultState);
        }
    }

    public MachineDefinition getDefinition() {
        return definition;
    }

    public RotationState getRotationState() {
        return definition.getRotationState();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return getDefinition().getBlockEntityType().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        MachineDefinition built = MachineDefinition.getBuilt();
        if (built == null) {
            return;
        }
        RotationState rotationState = built.getRotationState();
        if (rotationState != RotationState.NONE) {
            builder.add(rotationState.property);
            if (built.isAllowExtendedFacing()) {
                builder.add(EFBlockStateProperties.UPWARDS_FACING);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = getRotationState() == RotationState.NONE ? definition.getShape(Direction.NORTH) :
                definition.getShape(state.getValue(getRotationState().property));
        if (level.getBlockEntity(pos) instanceof MetaMachine machine) {
            VoxelShape[] coverShapes = machine.getCoverContainer().addCoverCollisionBoundingBox();
            for (VoxelShape coverShape : coverShapes) {
                shape = net.minecraft.world.phys.shapes.Shapes.or(shape, coverShape);
            }
        }
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            machine.animateTick(random);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack) {
        if (!level.isClientSide) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                machine.onMachinePlaced(player, stack);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.updateNeighbourForOutputSignal(pos, this);
        level.invalidateCapabilities(pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                   boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            machine.getCoverContainer().onNeighborChanged(block, fromPos, isMoving);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        RotationState rotationState = getRotationState();
        var player = context.getPlayer();
        var blockPos = context.getClickedPos();
        var state = defaultBlockState();
        if (player != null && rotationState != RotationState.NONE) {
            if (rotationState == RotationState.Y_AXIS) {
                state = state.setValue(rotationState.property, Direction.UP);
            } else {
                state = state.setValue(rotationState.property, player.getDirection().getOpposite());
            }
            Vec3 pos = player.position();
            if (Math.abs(pos.x - ((float) blockPos.getX() + 0.5F)) < 2.0D &&
                    Math.abs(pos.z - ((float) blockPos.getZ() + 0.5F)) < 2.0D) {
                double eyeY = pos.y + player.getEyeHeight();
                if (eyeY - blockPos.getY() > 2.0D && rotationState.test(Direction.UP)) {
                    state = state.setValue(rotationState.property, Direction.UP);
                }
                if (blockPos.getY() - eyeY > 0.0D && rotationState.test(Direction.DOWN)) {
                    state = state.setValue(rotationState.property, Direction.DOWN);
                }
            }
            if (getDefinition().isAllowExtendedFacing()) {
                Direction frontFacing = state.getValue(rotationState.property);
                if (frontFacing == Direction.UP) {
                    state = state.setValue(EFBlockStateProperties.UPWARDS_FACING, player.getDirection());
                } else if (frontFacing == Direction.DOWN) {
                    state = state.setValue(EFBlockStateProperties.UPWARDS_FACING,
                            player.getDirection().getOpposite());
                }
            }
        }
        return state;
    }

    @Override
    public void appendHoverText(
                                ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        definition.getTooltipBuilder().accept(stack, tooltip);
        String key = "%s.machine.%s.tooltip".formatted(definition.getId().getNamespace(), definition.getId().getPath());
        if (Language.getInstance().has(key)) {
            tooltip.add(1, Component.translatable(key));
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (getRotationState() == RotationState.NONE) {
            return state;
        }
        return state.setValue(getRotationState().property, rotation.rotate(state.getValue(getRotationState().property)));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MetaMachine machine) {
            machine.modifyDrops(drops);
        }
        return drops;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity()) {
            if (!state.is(newState.getBlock())) {
                MetaMachine machine = MetaMachine.getMachine(level, pos);
                if (machine != null) {
                    machine.onMachineDestroyed();
                }
                level.updateNeighbourForOutputSignal(pos, this);
                level.invalidateCapabilities(pos);
                level.removeBlockEntity(pos);
            } else if (getRotationState() != RotationState.NONE) {
                Direction oldFacing = state.getValue(getRotationState().property);
                Direction newFacing = newState.getValue(getRotationState().property);
                if (newFacing != oldFacing) {
                    MetaMachine machine = MetaMachine.getMachine(level, pos);
                    if (machine != null) {
                        machine.onRotated(oldFacing, newFacing);
                    }
                }
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
                                              ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        return MetaMachine.getMachine(level, pos) == null ? ItemInteractionResult.FAIL : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
                                               BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return MetaMachine.getMachine(level, pos) == null ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
                                                                            Level level, BlockState state, BlockEntityType<T> type) {
        return (tickerLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (tickerLevel.isClientSide) {
                    machine.clientTick();
                } else {
                    machine.serverTick();
                    machine.updateTick();
                }
            }
        };
    }
}
