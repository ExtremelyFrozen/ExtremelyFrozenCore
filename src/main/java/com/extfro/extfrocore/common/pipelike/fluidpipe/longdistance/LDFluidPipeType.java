package com.extfro.extfrocore.common.pipelike.fluidpipe.longdistance;

import com.extfro.extfrocore.api.pipenet.longdistance.LongDistancePipeType;
import com.extfro.extfrocore.config.ConfigHolder;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldFluidPipeMinDistance;
    }
}
