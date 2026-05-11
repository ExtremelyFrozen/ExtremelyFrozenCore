package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;

import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class EFFluidStorageImpl implements EFFluidStorage {

    private final Map<EFFluidStorageKey, FluidEntry> map = new Object2ObjectOpenHashMap<>();
    private Map<EFFluidStorageKey, EFFluidBuilder> toRegister = new Object2ObjectOpenHashMap<>();
    private boolean registered = false;

    @Override
    public void enqueueRegistration(@NotNull EFFluidStorageKey key, @NotNull EFFluidBuilder builder) {
        if (registered) {
            throw new IllegalStateException("Cannot enqueue a builder after registration");
        }
        if (toRegister.containsKey(key)) {
            throw new IllegalArgumentException("FluidStorageKey " + key + " is already queued");
        }
        toRegister.put(key, builder);
    }

    @Override
    public @Nullable EFFluidBuilder getQueuedBuilder(@NotNull EFFluidStorageKey key) {
        if (registered) {
            throw new IllegalArgumentException("FluidStorage has already been registered");
        }
        return toRegister.get(key);
    }

    public void registerFluids(@NotNull EFMaterial material, @NotNull EFRegistrate registrate) {
        if (registered) {
            throw new IllegalStateException("FluidStorage has already been registered");
        }
        if (toRegister.isEmpty() && map.isEmpty()) {
            enqueueRegistration(EFFluidStorageKeys.LIQUID, new EFFluidBuilder());
        }
        toRegister.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getKey().getRegistrationPriority()))
                .forEach(entry -> {
                    if (map.containsKey(entry.getKey())) {
                        ExtForCore.LOGGER.warn("{} already has an associated fluid for material {}", entry.getKey(),
                                material);
                        return;
                    }
                    Supplier<? extends Fluid> fluid = entry.getValue().build(material, entry.getKey(), registrate);
                    store(entry.getKey(), fluid, entry.getValue());
                });
        toRegister = null;
        registered = true;
    }

    @Override
    public @Nullable Fluid get(@NotNull EFFluidStorageKey key) {
        return map.containsKey(key) ? map.get(key).getFluid().get() : null;
    }

    @Override
    public @Nullable FluidEntry getEntry(@NotNull EFFluidStorageKey key) {
        return map.get(key);
    }

    @Override
    public void store(@NotNull EFFluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
                      @Nullable EFFluidBuilder builder) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(key + " already has an associated fluid");
        }
        map.put(key, new FluidEntry(fluid, builder));
    }
}
