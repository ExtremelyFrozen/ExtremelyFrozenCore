package com.extfro.extfrocore.client;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.client.model.machine.MachineModelLoader;
import com.extfro.extfrocore.client.renderer.block.EFMaterialBlockRenderer;
import com.extfro.extfrocore.client.renderer.cover.CoverRenderers;
import com.extfro.extfrocore.client.renderer.item.EFMaterialItemRenderer;
import com.extfro.extfrocore.data.pack.EFDynamicResourcePack;
import com.extfro.extfrocore.data.pack.EFDynamicResourceRegistrar;
import com.extfro.extfrocore.data.pack.EFPackSource;
import com.extfro.extfrocore.data.pack.event.EFRegisterDynamicResourcesEvent;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class ClientProxy {

    static {
        EFDynamicResourceRegistrar.registerClient(event -> {
            EFMaterialBlockRenderer.reinitModels();
            EFMaterialItemRenderer.reinitModels();
            CoverRenderers.onResourceManagerReload();
        });
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientProxy::registerPackFinders);
        modBus.addListener(ClientProxy::registerDynamicAssets);
        modBus.addListener(ClientProxy::registerModelLoaders);
    }

    private static void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            EFDynamicResourcePack.clearClient();
            event.addRepositorySource(new EFPackSource(ExtForCore.MOD_ID + ":dynamic_assets",
                    event.getPackType(), Pack.Position.BOTTOM, EFDynamicResourcePack::new));
        }
    }

    private static void registerDynamicAssets(EFRegisterDynamicResourcesEvent event) {
        EFDynamicResourceRegistrar.generateClient(event);
    }

    private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
    }
}
