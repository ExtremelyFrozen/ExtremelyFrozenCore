package com.extfro.extfrocore.common;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.EFBlockCapabilities;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.api.sync_system.SyncedComponents;
import com.extfro.extfrocore.api.sync_system.network.ClientBlockEntitySyncPayload;
import com.extfro.extfrocore.api.sync_system.network.ServerBlockEntitySyncPayload;
import com.extfro.extfrocore.common.data.EFRecipeCapabilities;
import com.extfro.extfrocore.common.data.EFRecipeTypes;
import com.extfro.extfrocore.common.material.EFMaterialRegistration;
import com.extfro.extfrocore.common.registry.EFRegistration;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CommonProxy {

    private static IEventBus modBus;

    public static void init(final IEventBus modBus) {
        CommonProxy.modBus = modBus;
        EFRegistries.init(modBus);
        EFRecipeCapabilities.init();
        EFRecipeTypes.init();
        EFMaterialRegistration.init(modBus);
        EFRegistration.REGISTRATE.registerRegistrate(modBus);
        SyncedComponents.COMPONENTS.register(modBus);
        modBus.addListener(EFRegistries::registerRegistries);
        modBus.addListener(EFRegistries::registerDataPackRegistries);
        modBus.addListener(CommonProxy::registerCapabilities);
        modBus.addListener(CommonProxy::registerPayloadHandlers);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var definition : EFRegistries.MACHINES) {
            if (event.isBlockRegistered(EFBlockCapabilities.COVERABLE, definition.getBlock())) {
                continue;
            }
            event.registerBlock(EFBlockCapabilities.COVERABLE,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof com.extfro.extfrocore.api.machine.MetaMachine machine ?
                            machine.getCoverContainer() : null,
                    definition.getBlock());
            event.registerBlock(Capabilities.ItemHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof com.extfro.extfrocore.api.machine.MetaMachine machine ?
                            machine.getItemHandlerCap(side, true) : null,
                    definition.getBlock());
            event.registerBlock(Capabilities.FluidHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> blockEntity instanceof com.extfro.extfrocore.api.machine.MetaMachine machine ?
                            machine.getFluidHandlerCap(side, true) : null,
                    definition.getBlock());
        }
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ExtForCore.MOD_ID);
        registrar.playToClient(ServerBlockEntitySyncPayload.TYPE, ServerBlockEntitySyncPayload.CODEC,
                ServerBlockEntitySyncPayload::execute);
        registrar.playToServer(ClientBlockEntitySyncPayload.TYPE, ClientBlockEntitySyncPayload.CODEC,
                ClientBlockEntitySyncPayload::execute);
    }
}
