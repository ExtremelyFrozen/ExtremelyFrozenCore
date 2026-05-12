package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMaintenanceMachine;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.core.Direction;

public class MaintenanceDetectorCover extends DetectorCover {

    public MaintenanceDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        if (!ConfigHolder.INSTANCE.machines.enableMaintenance) {
            return false;
        }

        return super.canAttach() &&
                GTCapabilityHelper.getMaintenanceMachine(coverHolder.getLevel(), coverHolder.getBlockPos(),
                        attachedSide) !=
                        null;
    }

    @Override
    protected void update() {
        if (!shouldUpdate()) {
            return;
        }

        IMaintenanceMachine maintenance = GTCapabilityHelper.getMaintenanceMachine(coverHolder.getLevel(),
                coverHolder.getBlockPos(), attachedSide);

        int signal = getRedstoneSignalOutput();
        boolean shouldSignal = isInverted() != maintenance.hasMaintenanceProblems();

        if (shouldSignal && signal != 15) {
            setRedstoneSignalOutput(15);
        } else if (!shouldSignal && signal == 15) {
            setRedstoneSignalOutput(0);
        }
    }
}
