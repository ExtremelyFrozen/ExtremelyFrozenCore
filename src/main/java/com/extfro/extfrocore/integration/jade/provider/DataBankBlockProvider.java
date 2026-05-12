package com.extfro.extfrocore.integration.jade.provider;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.common.machine.multiblock.electric.research.DataBankMachine;
import com.extfro.extfrocore.utils.FormattingUtil;
import com.extfro.extfrocore.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class DataBankBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public ResourceLocation getUid() {
        return ExtForCore.id("data_bank");
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof DataBankMachine) {
            long energyUsage = blockAccessor.getServerData().getLong("energyUsage");
            String energyFormatted = FormattingUtil.formatNumbers(energyUsage);
            // wrap in text component to keep it from being formatted
            Component voltageName = Component.literal(EFValues.VNF[GTUtil.getTierByVoltage(energyUsage)]);
            Component text = Component.translatable(
                    "gtceu.multiblock.energy_consumption",
                    energyFormatted,
                    voltageName);

            iTooltip.add(text);
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof DataBankMachine dataBank) {
            compoundTag.putLong("energyUsage", dataBank.getEnergyUsage());
        }
    }
}
