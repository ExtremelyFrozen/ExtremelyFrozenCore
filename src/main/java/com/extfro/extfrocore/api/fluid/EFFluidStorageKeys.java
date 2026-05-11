package com.extfro.extfrocore.api.fluid;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.api.material.property.EFFluidProperty;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;

import org.jetbrains.annotations.NotNull;

public final class EFFluidStorageKeys {

    public static final EFFluidStorageKey LIQUID = new EFFluidStorageKey(ExtForCore.id("liquid"),
            null, EFMaterialIconType.liquid,
            material -> prefixedRegisteredName("liquid_", EFFluidStorageKeys.LIQUID, material),
            material -> material.hasProperty(EFMaterialPropertyKey.DUST) ?
                    "extfrocore.fluid.liquid_generic" : "extfrocore.fluid.generic",
            EFFluidState.LIQUID, 0);

    public static final EFFluidStorageKey GAS = new EFFluidStorageKey(ExtForCore.id("gas"),
            null, EFMaterialIconType.gas,
            material -> postfixedRegisteredName("_gas", EFFluidStorageKeys.GAS, material),
            material -> material.hasProperty(EFMaterialPropertyKey.DUST) ?
                    "extfrocore.fluid.gas_vapor" : "extfrocore.fluid.generic",
            EFFluidState.GAS, 0);

    public static final EFFluidStorageKey PLASMA = new EFFluidStorageKey(ExtForCore.id("plasma"),
            null, EFMaterialIconType.plasma,
            material -> material.getName() + "_plasma",
            material -> "extfrocore.fluid.plasma",
            EFFluidState.PLASMA, -1);

    public static final EFFluidStorageKey MOLTEN = new EFFluidStorageKey(ExtForCore.id("molten"),
            null, EFMaterialIconType.molten,
            material -> "molten_" + material.getName(),
            material -> "extfrocore.fluid.molten",
            EFFluidState.LIQUID, -1);

    private EFFluidStorageKeys() {}

    private static @NotNull String prefixedRegisteredName(@NotNull String prefix, @NotNull EFFluidStorageKey key,
                                                          @NotNull EFMaterial material) {
        EFFluidProperty property = material.getProperty(EFMaterialPropertyKey.FLUID);
        if (property != null && property.getPrimaryKey() != key) {
            return prefix + material.getName();
        }
        return material.getName();
    }

    private static @NotNull String postfixedRegisteredName(@NotNull String postfix, @NotNull EFFluidStorageKey key,
                                                           @NotNull EFMaterial material) {
        EFFluidProperty property = material.getProperty(EFMaterialPropertyKey.FLUID);
        if (property != null && property.getPrimaryKey() != key) {
            return material.getName() + postfix;
        }
        return material.getName();
    }
}
