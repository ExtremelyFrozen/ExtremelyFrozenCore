package com.extfro.extfrocore.api.fluid;

import net.minecraft.world.level.material.Fluid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface EFFluidStorage {

    @AllArgsConstructor
    class FluidEntry {

        @Getter
        private Supplier<? extends Fluid> fluid;
        @Nullable
        @Getter
        @Setter
        private EFFluidBuilder builder;
    }

    void enqueueRegistration(@NotNull EFFluidStorageKey key, @NotNull EFFluidBuilder builder);

    @Nullable
    EFFluidBuilder getQueuedBuilder(@NotNull EFFluidStorageKey key);

    @Nullable
    Fluid get(@NotNull EFFluidStorageKey key);

    @Nullable
    FluidEntry getEntry(@NotNull EFFluidStorageKey key);

    void store(@NotNull EFFluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
               @Nullable EFFluidBuilder builder);
}
