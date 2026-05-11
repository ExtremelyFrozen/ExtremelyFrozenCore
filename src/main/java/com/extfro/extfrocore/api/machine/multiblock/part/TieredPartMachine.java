package com.extfro.extfrocore.api.machine.multiblock.part;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.feature.ITieredMachine;

import lombok.Getter;

public class TieredPartMachine extends MultiblockPartMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
    }
}
