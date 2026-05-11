package com.extfro.extfrocore.integration.xei.circuit;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CircuitStackProviderRegistry {

    private static final List<ProviderEntry> PROVIDERS = new ArrayList<>();

    private CircuitStackProviderRegistry() {}

    public static void register(CircuitStackProvider provider) {
        register(0, provider);
    }

    public static void register(int priority, CircuitStackProvider provider) {
        PROVIDERS.add(new ProviderEntry(priority, provider));
        PROVIDERS.sort(Comparator.comparingInt(ProviderEntry::priority).reversed());
    }

    @Unmodifiable
    public static List<CircuitStackEntry> getStacks() {
        List<CircuitStackEntry> stacks = new ArrayList<>();
        for (ProviderEntry entry : PROVIDERS) {
            stacks.addAll(entry.provider().getStacks());
        }
        return List.copyOf(stacks);
    }

    public static boolean isEmpty() {
        return PROVIDERS.isEmpty();
    }

    private record ProviderEntry(int priority, CircuitStackProvider provider) {}
}
