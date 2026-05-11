package com.extfro.extfrocore.integration.emi;

import dev.emi.emi.api.EmiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EMIRegistrars {

    private static final List<Consumer<EmiRegistry>> REGISTRARS = new ArrayList<>();

    private EMIRegistrars() {}

    public static void registerRegistrar(Consumer<EmiRegistry> registrar) {
        REGISTRARS.add(registrar);
    }

    static void register(EmiRegistry registry) {
        REGISTRARS.forEach(registrar -> registrar.accept(registry));
    }
}
