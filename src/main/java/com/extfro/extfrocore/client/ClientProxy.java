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
import com.extfro.extfrocore.utils.input.SyncedKeyMapping;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class ClientProxy {

    public static void init(IEventBus modBus) {
        modBus.register(ClientProxy.class);
    }

    @SubscribeEvent
    public static void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            EFDynamicResourcePack.clearClient();
            event.addRepositorySource(new EFPackSource(ExtForCore.MOD_ID + ":dynamic_assets",
                    event.getPackType(), Pack.Position.BOTTOM, EFDynamicResourcePack::new));
        }
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        SyncedKeyMapping.onRegisterKeyBinds(event);
    }

    @SubscribeEvent
    public static void registerDynamicAssets(EFRegisterDynamicResourcesEvent event) {
        EFMaterialBlockRenderer.reinitModels();
        EFMaterialItemRenderer.reinitModels();
        CoverRenderers.onResourceManagerReload();
        EFDynamicResourceRegistrar.generateClient(event);
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
    }
}
