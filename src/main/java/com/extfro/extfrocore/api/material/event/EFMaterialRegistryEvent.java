package com.extfro.extfrocore.api.material.event;

import com.extfro.extfrocore.api.material.EFMaterialRegistryManager;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class EFMaterialRegistryEvent extends Event implements IModBusEvent {

    public EFMaterialRegistryManager getManager() {
        return com.extfro.extfrocore.common.material.EFMaterialRegistryManager.getInstance();
    }
}
