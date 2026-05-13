package com.extfro.extfrocore.api.machine.feature;

import com.extfro.extfrocore.api.machine.trait.NotifiableItemStackHandler;

public interface IHasCircuitSlot {

    default boolean isCircuitSlotEnabled() {
        return true;
    }

    NotifiableItemStackHandler getCircuitInventory();
}
