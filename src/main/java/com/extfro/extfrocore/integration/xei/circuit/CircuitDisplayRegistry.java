package com.extfro.extfrocore.integration.xei.circuit;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CircuitDisplayRegistry {

    private static final List<Supplier<CircuitDisplay>> DISPLAY_SUPPLIERS = new ArrayList<>();
    private static final List<Consumer<CircuitDisplay>> DISPLAY_REGISTRARS = new ArrayList<>();

    private CircuitDisplayRegistry() {}

    public static void registerDisplay(Supplier<CircuitDisplay> displaySupplier) {
        DISPLAY_SUPPLIERS.add(displaySupplier);
    }

    public static void registerDisplayRegistrar(Consumer<CircuitDisplay> registrar) {
        DISPLAY_REGISTRARS.add(registrar);
    }

    @Unmodifiable
    public static List<CircuitDisplay> getDisplays() {
        List<CircuitDisplay> displays = new ArrayList<>();
        for (Supplier<CircuitDisplay> supplier : DISPLAY_SUPPLIERS) {
            CircuitDisplay display = supplier.get();
            if (!display.isEmpty()) {
                displays.add(display);
            }
        }
        return List.copyOf(displays);
    }

    public static void registerDisplays() {
        for (CircuitDisplay display : getDisplays()) {
            for (Consumer<CircuitDisplay> registrar : DISPLAY_REGISTRARS) {
                registrar.accept(display);
            }
        }
    }
}
