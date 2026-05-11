package com.extfro.extfrocore.common.material;

import com.extfro.extfrocore.api.material.event.EFMaterialEvent;
import com.extfro.extfrocore.api.material.event.EFMaterialRegistryEvent;
import com.extfro.extfrocore.api.material.event.EFPostMaterialEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

public final class EFMaterialRegistration {

    public static void init(IEventBus modBus) {
        modBus.addListener(EFMaterialRegistration::construct);
    }

    private static void construct(FMLConstructModEvent event) {
        event.enqueueWork(EFMaterialRegistration::initMaterials);
    }

    private static void initMaterials() {
        EFMaterialRegistryManager manager = EFMaterialRegistryManager.getInstance();
        ModLoader.postEvent(new EFMaterialRegistryEvent());
        manager.unfreezeRegistries();
        ModLoader.postEvent(new EFMaterialEvent());
        manager.closeRegistries();
        manager.registerMaterialFluids();
        ModLoader.postEvent(new EFPostMaterialEvent());
        manager.freezeRegistries();
    }

    private EFMaterialRegistration() {}
}
