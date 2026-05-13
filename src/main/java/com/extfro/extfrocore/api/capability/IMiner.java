package com.extfro.extfrocore.api.capability;

import com.extfro.extfrocore.api.machine.feature.IRecipeLogicMachine;

public interface IMiner extends IRecipeLogicMachine {

    boolean drainInput(boolean simulate);

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
