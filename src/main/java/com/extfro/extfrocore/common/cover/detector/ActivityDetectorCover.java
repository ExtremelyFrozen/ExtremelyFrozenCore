package com.extfro.extfrocore.common.cover.detector;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.capability.IWorkable;
import com.extfro.extfrocore.api.cover.CoverDefinition;

import net.minecraft.core.Direction;

public class ActivityDetectorCover extends DetectorCover {

    public ActivityDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getWorkable() != null;
    }

    @Override
    protected void update() {
        if (!shouldUpdate()) {
            return;
        }

        var workable = getWorkable();
        if (workable == null) {
            setRedstoneSignalOutput(0);
            return;
        }

        boolean isCurrentlyWorking = workable.isActive() && workable.isWorkingEnabled();

        setRedstoneSignalOutput(isCurrentlyWorking != isInverted() ? 15 : 0);
    }

    protected IWorkable getWorkable() {
        var workable = GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide);
        if (workable == null) {
            workable = GTCapabilityHelper.getRecipeLogic(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide);
        }
        return workable;
    }
}
