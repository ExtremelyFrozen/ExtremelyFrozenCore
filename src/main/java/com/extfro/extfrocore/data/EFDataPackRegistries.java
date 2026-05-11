package com.extfro.extfrocore.data;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects dynamic registry bootstraps used by datagen infrastructure.
 * Concrete content should register bootstraps from its own module, not here.
 */
public final class EFDataPackRegistries {

    private static final List<RegistryBootstrapEntry<?>> BOOTSTRAPS = new ArrayList<>();

    private EFDataPackRegistries() {}

    public static <T> void register(ResourceKey<? extends Registry<T>> registryKey,
                                    RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
        BOOTSTRAPS.add(new RegistryBootstrapEntry<>(registryKey, bootstrap));
    }

    public static boolean isEmpty() {
        return BOOTSTRAPS.isEmpty();
    }

    public static RegistrySetBuilder createBuilder() {
        RegistrySetBuilder builder = new RegistrySetBuilder();
        for (RegistryBootstrapEntry<?> entry : BOOTSTRAPS) {
            entry.addTo(builder);
        }
        return builder;
    }

    private record RegistryBootstrapEntry<T>(ResourceKey<? extends Registry<T>> registryKey,
                                             RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {

        private void addTo(RegistrySetBuilder builder) {
            builder.add(registryKey, bootstrap);
        }
    }
}
