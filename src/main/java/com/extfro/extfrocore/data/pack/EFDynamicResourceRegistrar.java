package com.extfro.extfrocore.data.pack;

import com.extfro.extfrocore.data.pack.event.EFRegisterDynamicResourcesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EFDynamicResourceRegistrar {

    private static final List<Consumer<EFRegisterDynamicResourcesEvent>> CLIENT_GENERATORS = new ArrayList<>();

    private EFDynamicResourceRegistrar() {
    }

    public static void registerClient(Consumer<EFRegisterDynamicResourcesEvent> generator) {
        CLIENT_GENERATORS.add(generator);
    }

    public static void generateClient(EFRegisterDynamicResourcesEvent event) {
        for (Consumer<EFRegisterDynamicResourcesEvent> generator : CLIENT_GENERATORS) {
            generator.accept(event);
        }
    }
}
