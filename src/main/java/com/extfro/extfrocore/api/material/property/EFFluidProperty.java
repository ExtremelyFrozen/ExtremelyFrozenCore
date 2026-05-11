package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.fluid.EFFluidBuilder;
import com.extfro.extfrocore.api.fluid.EFFluidStorage;
import com.extfro.extfrocore.api.fluid.EFFluidStorageImpl;
import com.extfro.extfrocore.api.fluid.EFFluidStorageKey;
import com.extfro.extfrocore.api.fluid.EFFluidStorageKeys;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.registry.registrate.EFRegistrate;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@NoArgsConstructor
public class EFFluidProperty implements EFMaterialProperty, EFFluidStorage {

    private final EFFluidStorageImpl storage = new EFFluidStorageImpl();
    @Getter
    @Setter
    private EFFluidStorageKey primaryKey = null;
    @Setter
    private @Nullable Fluid solidifyingFluid = null;

    public EFFluidProperty(@NotNull EFFluidStorageKey key, @NotNull EFFluidBuilder builder) {
        enqueueRegistration(key, builder);
    }

    public @NotNull EFFluidStorage getStorage() {
        return this;
    }

    public void registerFluids(@NotNull EFMaterial material, @NotNull EFRegistrate registrate) {
        this.storage.registerFluids(material, registrate);
    }

    @Override
    public void enqueueRegistration(@NotNull EFFluidStorageKey key, @NotNull EFFluidBuilder builder) {
        storage.enqueueRegistration(key, builder);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public void store(@NotNull EFFluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
                      @Nullable EFFluidBuilder builder) {
        storage.store(key, fluid, builder);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public @Nullable Fluid get(@NotNull EFFluidStorageKey key) {
        return storage.get(key);
    }

    @Override
    public @Nullable FluidEntry getEntry(@NotNull EFFluidStorageKey key) {
        return storage.getEntry(key);
    }

    @Override
    public @Nullable EFFluidBuilder getQueuedBuilder(@NotNull EFFluidStorageKey key) {
        return storage.getQueuedBuilder(key);
    }

    public @Nullable Fluid solidifiesFrom() {
        if (this.solidifyingFluid == null) {
            this.solidifyingFluid = getStorage().get(EFFluidStorageKeys.LIQUID);
        }
        return solidifyingFluid;
    }

    public @NotNull FluidStack solidifiesFrom(int amount) {
        Fluid fluid = solidifiesFrom();
        return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        if (this.primaryKey == null) {
            throw new IllegalStateException("FluidProperty cannot be empty");
        }
    }
}
