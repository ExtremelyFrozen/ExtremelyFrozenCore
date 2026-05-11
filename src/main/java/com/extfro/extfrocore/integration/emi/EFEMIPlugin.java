package com.extfro.extfrocore.integration.emi;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.emi.category.EmiXEIRegistrars;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EFEMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (ExtForCore.Mods.isEMILoaded()) {
            EmiXEIRegistrars.register(registry);
        }
    }
}
