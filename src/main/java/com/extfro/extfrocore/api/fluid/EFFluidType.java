package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.api.material.EFMaterial;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class EFFluidType extends FluidType {

    private final String translationKey;
    private final EFMaterial material;

    public EFFluidType(Properties properties, String translationKey, EFMaterial material) {
        super(properties);
        this.translationKey = translationKey;
        this.material = material;
    }

    @Override
    public String getDescriptionId() {
        return material.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(translationKey, material.getLocalizedName());
    }

    @Override
    public Component getDescription(FluidStack stack) {
        return getDescription();
    }
}
