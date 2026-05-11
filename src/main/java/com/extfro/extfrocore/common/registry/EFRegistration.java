package com.extfro.extfrocore.common.registry;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class EFRegistration {

    public static final EFRegistrate REGISTRATE = EFRegistrate.create(ExtForCore.MOD_ID);

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private EFRegistration() {}
}
