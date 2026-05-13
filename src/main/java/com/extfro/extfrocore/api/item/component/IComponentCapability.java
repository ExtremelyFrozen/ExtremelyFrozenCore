package com.extfro.extfrocore.api.item.component;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public interface IComponentCapability {

    void attachCapabilities(RegisterCapabilitiesEvent event, Item item);
}
