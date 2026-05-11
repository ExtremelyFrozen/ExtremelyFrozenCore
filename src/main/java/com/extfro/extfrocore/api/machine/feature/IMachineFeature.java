package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.machine.MetaMachine;

public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
