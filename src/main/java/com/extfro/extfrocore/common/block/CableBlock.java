package com.extfro.extfrocore.common.block;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.block.MaterialPipeBlock;
import com.extfro.extfrocore.api.blockentity.PipeBlockEntity;
import com.extfro.extfrocore.api.capability.GTCapability;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.properties.PropertyKey;
import com.extfro.extfrocore.api.data.chemical.material.properties.WireProperties;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.api.pipenet.IPipeNode;
import com.extfro.extfrocore.api.registry.registrate.provider.GTBlockstateProvider;
import com.extfro.extfrocore.client.model.pipe.PipeModel;
import com.extfro.extfrocore.common.blockentity.CableBlockEntity;
import com.extfro.extfrocore.common.data.GTBlockEntities;
import com.extfro.extfrocore.common.data.GTDamageTypes;
import com.extfro.extfrocore.common.data.GTMaterialBlocks;
import com.extfro.extfrocore.common.pipelike.cable.Insulation;
import com.extfro.extfrocore.common.pipelike.cable.LevelEnergyNet;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableBlock extends MaterialPipeBlock<Insulation, WireProperties, LevelEnergyNet> {

    public CableBlock(Properties properties, Insulation insulation, Material material) {
        super(properties, insulation, material);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof CableBlockEntity cableBlockEntity) {
                return cableBlockEntity.getEnergyContainer(side);
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
    public int tinted(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (pipeType.isCable && (index == 0 || index == 2)) {
            return 0x404040;
        }
        return super.tinted(state, level, pos, index);
    }

    @Override
    protected WireProperties createProperties(Insulation insulation, Material material) {
        return insulation.modifyProperties(material.getProperty(PropertyKey.WIRE));
    }

    @Override
    protected WireProperties createMaterialData() {
        return material.getProperty(PropertyKey.WIRE);
    }

    @Override
    public LevelEnergyNet getWorldPipeNet(ServerLevel level) {
        return LevelEnergyNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<Insulation, WireProperties>> getBlockEntityType() {
        return GTBlockEntities.CABLE.get();
    }

    @Override
    public boolean canPipesConnect(IPipeNode<Insulation, WireProperties> selfTile, Direction side,
                                   IPipeNode<Insulation, WireProperties> sideTile) {
        return selfTile instanceof CableBlockEntity && sideTile instanceof CableBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<Insulation, WireProperties> selfTile,
                                         Direction side, Level level, BlockPos pos) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, pos, side.getOpposite()) != null;
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        WireProperties wireProperties = createProperties(defaultBlockState(), stack);
        int tier = GTUtil.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor())
            tooltip.add(Component.translatable("gtceu.cable.superconductor", EFValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage",
                FormattingUtil.formatNumbers(wireProperties.getVoltage()), EFValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage",
                FormattingUtil.formatNumbers(wireProperties.getAmperage())));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block",
                FormattingUtil.formatNumbers(wireProperties.getLossPerBlock())));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        var pipeNode = getPipeTile(level, pos);
        if (pipeNode == null) {
            ExtForCore.LOGGER.error("Pipe was null");
            return;
        }
        if (!pipeNode.getFrameMaterial().isNull()) {
            BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                    .getDefaultState();
            frameState.entityInside(level, pos, entity);
            return;
        }
        if (level.isClientSide) return;

        Insulation insulation = getPipeTile(level, pos).getPipeType();
        if (insulation.insulationLevel == -1 && entity instanceof LivingEntity entityLiving) {
            CableBlockEntity cable = (CableBlockEntity) getPipeTile(level, pos);
            if (cable != null && cable.getFrameMaterial().isNull() &&
                    cable.getNodeData().getLossPerBlock() > 0) {
                long voltage = cable.getCurrentMaxVoltage();
                double amperage = cable.getAverageAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (float) ((GTUtil.getTierByVoltage(voltage) + 1) * amperage * 4);
                    entityLiving.hurt(level.damageSources().source(GTDamageTypes.ELECTRIC), damageAmount);
                    if (entityLiving instanceof ServerPlayer) {
                        // TODO advancments
                        // AdvancementTriggers.ELECTROCUTION_DEATH.trigger((ServerPlayer) entityLiving);
                    }
                }
            }
        }
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}
