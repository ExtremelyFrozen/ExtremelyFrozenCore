package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.IEnergyInfoProvider;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.utils.RedstoneUtil;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

public class EnergyDetectorCover extends DetectorCover {

    public EnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getEnergyInfoProvider() != null;
    }

    @Override
    protected void update() {
        if (!shouldUpdate())
            return;

        IEnergyInfoProvider energyInfoProvider = getEnergyInfoProvider();
        if (energyInfoProvider == null) return;

        var energyInfo = energyInfoProvider.getEnergyInfo();
        var isBigInt = energyInfoProvider.supportsBigIntEnergyValues();

        if (isBigInt) {
            if (energyInfo.capacity().equals(BigInteger.ZERO)) return;

            setRedstoneSignalOutput(
                    RedstoneUtil.computeRedstoneValue(energyInfo.stored(), energyInfo.capacity(), isInverted()));
        } else {
            long storedEnergy = energyInfo.stored().longValue();
            long energyCapacity = energyInfo.capacity().longValue();
            if (energyCapacity == 0) return;

            setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedEnergy, energyCapacity, isInverted()));
        }
    }

    @Nullable
    protected IEnergyInfoProvider getEnergyInfoProvider() {
        return GTCapabilityHelper.getEnergyInfoProvider(coverHolder.getLevel(), coverHolder.getBlockPos(),
                attachedSide);
    }
}
