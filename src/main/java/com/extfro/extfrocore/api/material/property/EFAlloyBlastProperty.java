package com.extfro.extfrocore.api.material.property;

import net.minecraft.world.level.material.Fluid;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EFAlloyBlastProperty implements EFMaterialProperty {

    private Supplier<? extends Fluid> fluidSupplier;
    private int temperature;
    @Getter
    @Setter
    @NotNull
    private Object recipeProducer = new Object();

    public EFAlloyBlastProperty(int temperature) {
        this.temperature = temperature;
    }

    public EFAlloyBlastProperty() {
        this(0);
    }

    @Override
    public void verifyProperty(EFMaterialProperties materialProperties) {
        materialProperties.ensureSet(EFMaterialPropertyKey.BLAST);
        materialProperties.ensureSet(EFMaterialPropertyKey.FLUID);
        this.temperature = materialProperties.getProperty(EFMaterialPropertyKey.BLAST).getBlastTemperature();
    }

    public void setFluid(@NotNull Supplier<? extends Fluid> materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.fluidSupplier = materialFluid;
    }

    public Fluid getFluid() {
        return fluidSupplier.get();
    }

    public void setTemperature(int fluidTemperature) {
        Preconditions.checkArgument(fluidTemperature > 0, "Invalid temperature");
        this.temperature = fluidTemperature;
    }

    public int getTemperature() {
        return temperature;
    }
}
