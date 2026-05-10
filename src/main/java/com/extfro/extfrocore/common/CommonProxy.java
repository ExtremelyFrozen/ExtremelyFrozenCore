package com.extfro.extfrocore.common;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.sync_system.SyncedComponents;
import com.extfro.extfrocore.api.sync_system.network.ClientBlockEntitySyncPayload;
import com.extfro.extfrocore.api.sync_system.network.ServerBlockEntitySyncPayload;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CommonProxy {

    private static IEventBus modBus;

    public static void init(final IEventBus modBus) {
        CommonProxy.modBus = modBus;
        SyncedComponents.COMPONENTS.register(modBus);
        modBus.addListener(CommonProxy::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ExtForCore.MOD_ID);
        registrar.playToClient(ServerBlockEntitySyncPayload.TYPE, ServerBlockEntitySyncPayload.CODEC,
                ServerBlockEntitySyncPayload::execute);
        registrar.playToServer(ClientBlockEntitySyncPayload.TYPE, ClientBlockEntitySyncPayload.CODEC,
                ClientBlockEntitySyncPayload::execute);
    }
}
