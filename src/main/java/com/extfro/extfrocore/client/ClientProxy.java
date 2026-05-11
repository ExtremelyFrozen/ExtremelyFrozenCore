package com.extfro.extfrocore.client;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.client.renderer.block.EFMaterialBlockRenderer;
import com.extfro.extfrocore.client.renderer.cover.MachineCoverBlockEntityRenderer;
import com.extfro.extfrocore.client.renderer.item.EFMaterialItemRenderer;
import com.extfro.extfrocore.data.pack.EFDynamicResourcePack;
import com.extfro.extfrocore.data.pack.EFDynamicResourceRegistrar;
import com.extfro.extfrocore.data.pack.EFPackSource;
import com.extfro.extfrocore.data.pack.event.EFRegisterDynamicResourcesEvent;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.HashSet;
import java.util.Set;

public class ClientProxy {

    static {
        EFDynamicResourceRegistrar.registerClient(event -> {
            EFMaterialBlockRenderer.reinitModels();
            EFMaterialItemRenderer.reinitModels();
        });
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientProxy::registerPackFinders);
        modBus.addListener(ClientProxy::registerDynamicAssets);
        modBus.addListener(ClientProxy::registerBlockEntityRenderers);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        Set<BlockEntityType<?>> registeredTypes = new HashSet<>();
        for (var definition : EFRegistries.MACHINES) {
            BlockEntityType<?> type = definition.getBlockEntityType();
            if (registeredTypes.add(type)) {
                event.registerBlockEntityRenderer((BlockEntityType<MetaMachine>) type,
                        MachineCoverBlockEntityRenderer::new);
            }
        }
    }
}
