package com.extfro.extfrocore.api.material.property;

public class EFWoodProperty implements EFMaterialProperty {

    @Override
    public void verifyProperty(EFMaterialProperties properties) {
        properties.ensureSet(EFMaterialPropertyKey.DUST, true);
    }
}
