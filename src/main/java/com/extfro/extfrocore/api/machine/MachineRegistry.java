package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.registry.EFRegistry;

import net.minecraft.resources.ResourceLocation;

public class MachineRegistry extends EFRegistry.ResourceKey<MachineDefinition> {

    public MachineRegistry(ResourceLocation registryName) {
        super(registryName);
        unfreeze();
    }
}
