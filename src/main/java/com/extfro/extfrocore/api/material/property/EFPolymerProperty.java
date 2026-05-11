package com.extfro.extfrocore.api.material.property;

import com.extfro.extfrocore.api.material.info.EFMaterialFlags;

public class EFPolymerProperty implements EFMaterialProperty {

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.INGOT, true);
        properties.getMaterial().addFlags(EFMaterialFlags.FLAMMABLE, EFMaterialFlags.NO_SMELTING);
    }
}
