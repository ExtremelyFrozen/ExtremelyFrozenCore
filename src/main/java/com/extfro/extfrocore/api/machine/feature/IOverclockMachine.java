package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.EFValues;

public interface IOverclockMachine extends IMachineFeature {

    int getOverclockTier();

    void setOverclockTier(int tier);

    int getMaxOverclockTier();

    int getMinOverclockTier();

    default long getOverclockVoltage() {
        return EFValues.V[getOverclockTier()];
    }
}
