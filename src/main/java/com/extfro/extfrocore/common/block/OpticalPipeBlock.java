package com.extfro.extfrocore.common.block;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.block.PipeBlock;
import com.extfro.extfrocore.api.block.property.GTBlockStateProperties;
import com.extfro.extfrocore.api.blockentity.PipeBlockEntity;
import com.extfro.extfrocore.api.capability.GTCapability;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.pipenet.IPipeNode;
import com.extfro.extfrocore.api.registry.registrate.provider.GTBlockstateProvider;
import com.extfro.extfrocore.client.model.pipe.ActivablePipeModel;
import com.extfro.extfrocore.client.model.pipe.PipeModel;
import com.extfro.extfrocore.common.blockentity.OpticalPipeBlockEntity;
import com.extfro.extfrocore.common.data.GTBlockEntities;
import com.extfro.extfrocore.common.pipelike.optical.LevelOpticalPipeNet;
import com.extfro.extfrocore.common.pipelike.optical.OpticalPipeProperties;
import com.extfro.extfrocore.common.pipelike.optical.OpticalPipeType;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpticalPipeBlock extends PipeBlock<OpticalPipeType, OpticalPipeProperties, LevelOpticalPipeNet> {

    private final OpticalPipeProperties properties;

    public OpticalPipeBlock(BlockBehaviour.Properties properties, OpticalPipeType pipeType) {
        super(properties, pipeType);
        this.properties = OpticalPipeProperties.INSTANCE;

        registerDefaultState(defaultBlockState().setValue(GTBlockStateProperties.ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GTBlockStateProperties.ACTIVE);
    }

    @Override
    public @NotNull PipeModel createPipeModel(GTBlockstateProvider provider) {
        ActivablePipeModel pipeModel = new ActivablePipeModel(this, pipeType.getThickness(),
                ExtForCore.id("block/pipe/pipe_optical_side"), ExtForCore.id("block/pipe/pipe_optical_in"),
                provider);
        pipeModel.setSideOverlay(ExtForCore.id("block/pipe/pipe_optical_side_overlay"));
        pipeModel.setSideOverlayActive(ExtForCore.id("block/pipe/pipe_optical_side_overlay_active"));
        return pipeModel;
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_DATA_ACCESS, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof OpticalPipeBlockEntity opticalPipeBlockEntity) {
                if (level.isClientSide) {
                    return opticalPipeBlockEntity.getClientDataHandler();
                }
                if (opticalPipeBlockEntity.getHandlers().isEmpty()) {
                    opticalPipeBlockEntity.initHandlers();
                }
                opticalPipeBlockEntity.checkNetwork();
                return opticalPipeBlockEntity.getHandlers().getOrDefault(side,
                        opticalPipeBlockEntity.getDefaultHandler());
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof OpticalPipeBlockEntity opticalPipeBlockEntity) {
                if (level.isClientSide) {
                    return opticalPipeBlockEntity.getClientComputationHandler();
                }
                if (opticalPipeBlockEntity.getHandlers().isEmpty()) {
                    opticalPipeBlockEntity.initHandlers();
                }
                opticalPipeBlockEntity.checkNetwork();
                return opticalPipeBlockEntity.getHandlers().getOrDefault(side,
                        opticalPipeBlockEntity.getDefaultHandler());
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
                return pipe.getCoverContainer();
            }
            return null;
        }, this);
    }

    @Override
    public LevelOpticalPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelOpticalPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<OpticalPipeType, OpticalPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.OPTICAL_PIPE.get();
    }

    @Override
    public OpticalPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return null;
    }

    @Override
    public OpticalPipeProperties createProperties(@NotNull IPipeNode<OpticalPipeType, OpticalPipeProperties> pipeTile) {
        OpticalPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public OpticalPipeProperties getFallbackType() {
        return OpticalPipeProperties.INSTANCE;
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null &&
                    level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (!pipe.getFrameMaterial().isNull()) {
                    if (index == 3) {
                        return pipe.getFrameMaterial().getMaterialRGB();
                    } else if (index == 4) {
                        return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                    }
                }
                if (pipe.isPainted()) {
                    return pipe.getRealColor();
                }
            }
            return -1;
        };
    }

    @Override
    public boolean canPipesConnect(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side,
                                   IPipeNode<OpticalPipeType, OpticalPipeProperties> sideTile) {
        return selfTile instanceof OpticalPipeBlockEntity && sideTile instanceof OpticalPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<OpticalPipeType, OpticalPipeProperties> selfTile, Direction side,
                                         Level level, BlockPos pos) {
        if (level.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, pos, side.getOpposite()) != null) return true;
        return level.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, pos, side.getOpposite()) != null;
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}
