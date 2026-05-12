package com.extfro.extfrocore.common.machine.multiblock.part;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMaintenanceMachine;
import com.extfro.extfrocore.api.machine.multiblock.part.TieredPartMachine;

public class AutoMaintenanceHatchPartMachine extends TieredPartMachine implements IMaintenanceMachine {

    public AutoMaintenanceHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, EFValues.HV);
    }

    @Override
    public void setTaped(boolean ignored) {}

    @Override
    public boolean isTaped() {
        return false;
    }

    @Override
    public boolean isFullAuto() {
        return true;
    }

    @Override
    public byte startProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public byte getMaintenanceProblems() {
        return NO_PROBLEMS;
    }

    @Override
    public void setMaintenanceProblems(byte problems) {}

    @Override
    public int getTimeActive() {
        return 0;
    }

    @Override
    public void setTimeActive(int time) {}
}
