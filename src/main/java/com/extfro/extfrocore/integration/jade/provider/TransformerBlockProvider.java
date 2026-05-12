package com.extfro.extfrocore.integration.jade.provider;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.common.machine.electric.TransformerMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TransformerBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public ResourceLocation getUid() {
        return ExtForCore.id("transformer");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof TransformerMachine transformer) {
            compoundTag.putInt("side", transformer.getFrontFacing().get3DDataValue());
            compoundTag.putBoolean("transformUp", transformer.isTransformUp());
            compoundTag.putInt("baseAmp", transformer.getBaseAmp());
            compoundTag.putInt("baseVoltage", transformer.getTier());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof TransformerMachine transformer) {
            boolean transformUp = blockAccessor.getServerData().getBoolean("transformUp");
            int voltage = blockAccessor.getServerData().getInt("baseVoltage");
            int amp = blockAccessor.getServerData().getInt("baseAmp");
            if (transformUp) {
                tooltip.add(Component.translatable("gtceu.top.transform_up",
                        (EFValues.VNF[voltage] + " §r(" + amp * 4 + "A) -> " + EFValues.VNF[voltage + 1] + " §r(" +
                                amp +
                                "A)")));
            } else {
                tooltip.add(Component.translatable("gtceu.top.transform_down",
                        (EFValues.VNF[voltage + 1] + " §r(" + amp + "A) -> " + EFValues.VNF[voltage] + " §r(" +
                                amp * 4 +
                                "A)")));
            }

            if (blockAccessor.getHitResult().getDirection() ==
                    Direction.from3DDataValue(blockAccessor.getServerData().getInt("side"))) {
                tooltip.add(
                        Component.translatable(
                                (transformUp ? "gtceu.top.transform_output" : "gtceu.top.transform_input"),
                                (EFValues.VNF[voltage + 1] + " §r(" + amp + "A)")));
            } else {
                tooltip.add(
                        Component.translatable(
                                (transformUp ? "gtceu.top.transform_input" : "gtceu.top.transform_output"),
                                (EFValues.VNF[voltage] + " §r(" + amp * 4 + "A)")));
            }
        }
    }
}
