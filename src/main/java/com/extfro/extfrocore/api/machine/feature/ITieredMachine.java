package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.EFValues;

public interface ITieredMachine extends IMachineFeature {

    /**
     * Tier of machine determines it's input voltage, storage and generation rate
     *
     * @return tier of this machine
     */
    default int getTier() {
        return self().getDefinition().getTier();
    }

    default long getMaxVoltage() {
        return EFValues.V[getTier()];
    }
}
