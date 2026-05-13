package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.SimpleTieredMachine;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;

public class RockCrusherMachine extends SimpleTieredMachine {

    public RockCrusherMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, GTMachineUtils.defaultTankSizeFunction);
        environmentalExplosionTrait.setEnableEnvironmentalExplosions(false);
    }
}
