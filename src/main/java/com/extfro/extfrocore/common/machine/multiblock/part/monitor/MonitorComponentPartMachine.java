package com.extfro.extfrocore.common.machine.multiblock.part.monitor;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.IMonitorComponent;
import com.extfro.extfrocore.api.machine.multiblock.part.MultiblockPartMachine;

public abstract class MonitorComponentPartMachine extends MultiblockPartMachine implements IMonitorComponent {

    public MonitorComponentPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }
}
