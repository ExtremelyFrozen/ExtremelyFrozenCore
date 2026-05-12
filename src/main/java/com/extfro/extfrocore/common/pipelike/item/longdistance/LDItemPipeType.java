package com.extfro.extfrocore.common.pipelike.item.longdistance;

import com.extfro.extfrocore.api.pipenet.longdistance.LongDistancePipeType;
import com.extfro.extfrocore.config.ConfigHolder;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance;
    }
}
