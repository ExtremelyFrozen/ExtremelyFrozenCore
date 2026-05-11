package com.extfro.extfrocore.common.registry;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MachineRegistry;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class EFRegistration {

    public static final EFRegistrate REGISTRATE = EFRegistrate.create(ExtForCore.MOD_ID);
    public static final MachineRegistry MACHINES = new MachineRegistry(
            ResourceLocation.fromNamespaceAndPath(ExtForCore.MOD_ID, "machine"));

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private EFRegistration() {}
}
